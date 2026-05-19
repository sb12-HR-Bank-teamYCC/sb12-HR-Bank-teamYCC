package com.codeit.hrbank.service;

import com.codeit.hrbank.common.config.FileConfig;
import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.dataBackup.DataBackupDto;
import com.codeit.hrbank.dto.error.HrBankException;
import com.codeit.hrbank.dto.page.CursorPageResponse;
import com.codeit.hrbank.entity.FileMetadata;
import com.codeit.hrbank.entity.backupStatus.BackupStatus;
import com.codeit.hrbank.entity.backupStatus.DataBackup;
import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.mapper.DataBackupMapper;
import com.codeit.hrbank.repository.ChangeLogRepository;
import com.codeit.hrbank.repository.DataBackupRepository;
import com.codeit.hrbank.repository.EmployeeRepository;
import com.codeit.hrbank.repository.FileMetadataRepository;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataBackupService {

  private static final int CHUNK_SIZE = 1000;
  private static final String CSV_HEADER = "id,name,email,employeeNumber,department,position,hireDate,status";

  private final DataBackupRepository dataBackupRepository;
  private final ChangeLogRepository changeLogRepository;
  private final EmployeeRepository employeeRepository;
  private final FileMetadataRepository fileMetadataRepository;
  private final DataBackupMapper dataBackupMapper;
  private final FileConfig fileConfig;

  // ─────────────────────────────────────────────────────────────────────────────
  // 데이터 백업 프로세스 (STEP 1 ~ 4)
  // ─────────────────────────────────────────────────────────────────────────────
  @Transactional
  public DataBackupDto backup(String worker) {

    // STEP.1: 백업 필요 여부 판단
    if (!isBackupNeeded()) {
      log.info("[Backup] 변경 이력 없음 → SKIPPED 처리");
      return dataBackupMapper.toDto(saveSkipped(worker));
    }

    // STEP.2: IN_PROGRESS 이력 등록 (이 시점부터 API로 조회 가능)
    DataBackup backup = saveInProgress(worker);
    log.info("[Backup] 백업 시작, id={}, worker={}", backup.getId(), worker);

    Path tempCsvPath = null;
    try {
      // STEP.3: CSV 파일 생성 (청크 단위 처리)
      tempCsvPath = buildCsvPath(backup.getId());
      writeCsvInChunks(tempCsvPath);

      // STEP.4-1: 성공 → FileMetadata 저장 후 completed
      FileMetadata csvMeta = saveFileMetadata(tempCsvPath, "text/csv", "BACKUP");
      backup.complete(Instant.now(), csvMeta);
      DataBackup completed = dataBackupRepository.save(backup);
      log.info("[Backup] 백업 성공, id={}, file={}", completed.getId(), tempCsvPath.getFileName());
      return dataBackupMapper.toDto(completed);

    } catch (Exception e) {
      log.error("[Backup] 백업 실패, id={}", backup.getId(), e);

      // STEP.4-2: 실패 → 임시 CSV 삭제 후 에러 로그 파일 저장
      deleteQuietly(tempCsvPath);
      FileMetadata logMeta = saveErrorLog(backup.getId(), e);
      backup.fail(Instant.now(), logMeta);
      return dataBackupMapper.toDto(dataBackupRepository.save(backup));
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // GET /api/backups
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public CursorPageResponse<DataBackupDto> getBackups(
      String worker, String status,
      Instant startedAtFrom, Instant startedAtTo,
      UUID idAfter, String cursor, int size,
      String sortField, String sortDirection
  ) {
    BackupStatus backupStatus = parseStatus(status);

    List<DataBackup> rows = dataBackupRepository.findAllWithFilters(
        worker, backupStatus, startedAtFrom, startedAtTo,
        idAfter, cursor, size, sortField, sortDirection
    );

    boolean hasNext = rows.size() > size;
    List<DataBackup> content = hasNext ? rows.subList(0, size) : rows;

    String nextCursor = null;
    String nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      DataBackup last = content.get(content.size() - 1);
      nextCursor = encodeCursor(last, sortField);
      nextIdAfter = last.getId().toString();
    }

    long totalElements = dataBackupRepository.countWithFilters(
        worker, backupStatus, startedAtFrom, startedAtTo
    );

    return new CursorPageResponse<>(
        content.stream().map(dataBackupMapper::toDto).toList(),
        nextCursor, nextIdAfter, size, totalElements, hasNext
    );
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // GET /api/backups/latest
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public DataBackupDto getLatestBackup(String status) {
    BackupStatus backupStatus = (status == null || status.isBlank())
        ? BackupStatus.COMPLETED
        : BackupStatus.valueOf(status.toUpperCase());

    return dataBackupRepository
        .findLatestByStatus(backupStatus)
        .map(dataBackupMapper::toDto)
        .orElse(null);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: STEP 처리
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * STEP.1: 백업 필요 여부 판단
   * - 완료된 백업이 없으면 → true (최초 실행)
   * - endedAt이 null이면 → true (비정상 완료)
   * - 마지막 완료 이후 변경된 직원 데이터가 있으면 → true
   */
  private boolean isBackupNeeded() {
    return dataBackupRepository
        .findLatestByStatus(BackupStatus.COMPLETED)
        .map(last -> {
          Instant lastEndedAt = last.getEndedAt();
          if (lastEndedAt == null) {
            return true; // 비정상 케이스: endedAt이 없으면 백업 필요
          }
          return changeLogRepository.existsByAtAfter(lastEndedAt);
        })
        .orElse(true); // 완료된 백업 이력 자체가 없으면 백업 필요
  }

  private DataBackup saveSkipped(String worker) {
    return dataBackupRepository.save(DataBackup.builder()
        .worker(worker)
        .startedAt(Instant.now())
        .endedAt(Instant.now())
        .status(BackupStatus.SKIPPED)
        .build());
  }

  private DataBackup saveInProgress(String worker) {
    return dataBackupRepository.save(DataBackup.builder()
        .worker(worker)
        .startedAt(Instant.now())
        .status(BackupStatus.IN_PROGRESS)
        .build());
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: CSV 백업 파일 생성 (STEP.3)
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 전체 직원을 CHUNK_SIZE(1000)건씩 읽어 CSV로 기록합니다.
   * 한 번에 전체 로드 시 OOM 위험이 있으므로 페이징 방식으로 처리합니다.
   */
  private void writeCsvInChunks(Path csvPath) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
      writer.write('\uFEFF'); // UTF-8 BOM
      writer.write(CSV_HEADER);
      writer.newLine();

      int page = 0;
      Page<Employee> chunk;
      do {
        chunk = employeeRepository.findAllWithDepartmentForBackup(
            PageRequest.of(page++, CHUNK_SIZE)
        );
        for (Employee e : chunk.getContent()) {
          writer.write(toCsvRow(e));
          writer.newLine();
        }
        log.debug("[Backup] CSV 청크 처리, page={}, count={}", page - 1, chunk.getNumberOfElements());
      } while (chunk.hasNext());
    }
  }

  /**
   * Employee → CSV 한 줄 변환.
   * 필드 내 콤마/큰따옴표는 RFC 4180에 따라 큰따옴표로 감쌉니다.
   */
  private String toCsvRow(Employee e) {
    return String.join(",",
        csvEscape(e.getId().toString()),
        csvEscape(e.getName()),
        csvEscape(e.getEmail()),
        csvEscape(e.getEmployeeNumber()),
        csvEscape(e.getDepartment().getName()),
        csvEscape(e.getPosition()),
        csvEscape(e.getHireDate().toString()),
        csvEscape(e.getStatus().name())
    );
  }

  private String csvEscape(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: 에러 로그 파일 저장 (STEP.4-2)
  // ─────────────────────────────────────────────────────────────────────────────

  private FileMetadata saveErrorLog(UUID backupId, Exception e) {
    Path logPath = buildLogPath(backupId);
    try {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String logContent = "[Backup FAILED] id=" + backupId
          + ", time=" + Instant.now() + "\n" + sw;

      Files.writeString(logPath, logContent, StandardCharsets.UTF_8);
      return saveFileMetadata(logPath, "text/plain", "BACKUP_LOG");

    } catch (IOException ioEx) {
      log.error("[Backup] 에러 로그 파일 저장 실패, id={}", backupId, ioEx);
      return null;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: FileMetadata 저장
  // ─────────────────────────────────────────────────────────────────────────────

  private FileMetadata saveFileMetadata(Path filePath, String contentType, String fileType)
      throws IOException {
    long fileSize = Files.size(filePath);
    String fileName = filePath.getFileName().toString();

    FileMetadata metadata = FileMetadata.builder()
        .originalName(fileName)
        .storedName(fileName)
        .contentType(contentType)
        .size(fileSize)
        .fileType(fileType)
        .build();

    return fileMetadataRepository.save(metadata);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: 파일 경로 생성 / 유틸
  // ─────────────────────────────────────────────────────────────────────────────

  private Path buildCsvPath(UUID backupId) {
    return fileConfig.getUploadDir().resolve("backup_" + backupId + ".csv");
  }

  private Path buildLogPath(UUID backupId) {
    return fileConfig.getUploadDir().resolve("backup_" + backupId + "_error.log");
  }

  private void deleteQuietly(Path path) {
    if (path == null) return;
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      log.warn("[Backup] 임시 파일 삭제 실패: {}", path, e);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: 커서 인코딩 / 상태 파싱
  // ─────────────────────────────────────────────────────────────────────────────

  private String encodeCursor(DataBackup backup, String sortField) {
    String field = (sortField == null || sortField.isBlank()) ? "startedAt" : sortField;
    String sortValue = switch (field) {
      case "endedAt" -> backup.getEndedAt()   != null ? backup.getEndedAt().toString()   : "";
      case "status"  -> backup.getStatus()    != null ? backup.getStatus().name()         : "";
      default        -> backup.getStartedAt() != null ? backup.getStartedAt().toString() : "";
    };
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString((sortValue + ":" + backup.getId()).getBytes());
  }

  private BackupStatus parseStatus(String status) {
    if (status == null || status.isBlank()) return null;
    try {
      return BackupStatus.valueOf(status.toUpperCase());
    } catch (HrBankException e) {
      throw new HrBankException(ErrorCode.INVALID_STATUS);
    }
  }
}