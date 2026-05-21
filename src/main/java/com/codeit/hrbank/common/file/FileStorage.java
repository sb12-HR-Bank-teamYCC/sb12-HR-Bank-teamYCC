package com.codeit.hrbank.common.file;

import com.codeit.hrbank.common.config.FileConfig;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.file.FileTypeConst;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileStorage {

  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
  private static final DateTimeFormatter BACKUP_NAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  private final FileConfig fileConfig;

  // ─────────────────────────────────────────────────────────────────────────────
  // 프로필 이미지 저장 (uploads/images/)
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 프로필 이미지를 imageDir(uploads/images/)에 저장하고 FileMetadata를 반환합니다.
   * DB 저장은 포함하지 않습니다 — 호출 측(FileMetadataService)에서 persist 하세요.
   */
  public FileMetadata saveProfileImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
    }

    Path imageDir = fileConfig.getImageDir();
    String storedName = buildStoredName(file.getOriginalFilename());
    Path dest = imageDir.resolve(storedName);

    transferFile(file, dest);

    return FileMetadata.builder()
        .originalName(file.getOriginalFilename())
        .storedName(storedName)
        .contentType(file.getContentType())
        .size(file.getSize())
        .fileType(FileTypeConst.PROFILE_IMAGE)
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 일반 첨부 파일 저장 (uploads/)
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 일반 첨부 파일을 uploadDir(uploads/)에 저장하고 FileMetadata를 반환합니다.
   */
  public FileMetadata saveAttachFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
    }

    Path uploadDir = fileConfig.getUploadDir();
    String storedName = buildStoredName(file.getOriginalFilename());
    Path dest = uploadDir.resolve(storedName);

    transferFile(file, dest);

    return FileMetadata.builder()
        .originalName(file.getOriginalFilename())
        .storedName(storedName)
        .contentType(file.getContentType())
        .size(file.getSize())
        .fileType(FileTypeConst.PROFILE_IMAGE)
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 백업 파일 경로 제공 (uploads/backups/)
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 백업 CSV 파일 경로를 반환합니다.
   * 실제 파일 쓰기는 DataBackupService에서 수행합니다.
   */
  public Path buildBackupPath(UUID backupId) {
    String timestamp = LocalDateTime.now().format(BACKUP_NAME_FORMAT);
    String fileName = "backup_" + timestamp + "-" + backupId + ".csv";
    return resolveBackupDir().resolve(fileName);
  }

  /**
   * 백업 에러 로그 파일 경로를 반환합니다.
   */
  public Path buildBackupLogPath(UUID backupId) {
    String timestamp = LocalDateTime.now().format(BACKUP_NAME_FORMAT);
    String fileName = "backup_" + timestamp + "-" + backupId + ".log";
    return resolveBackupDir().resolve(fileName);
  }

  /**
   * 이미 디스크에 존재하는 파일 경로로 FileMetadata를 생성합니다.
   * DB 저장은 포함하지 않습니다 — 호출 측(FileMetadataService)에서 persist 하세요.
   */
  public FileMetadata metadataFromPath(Path filePath, String contentType, String fileType)
      throws IOException {
    String fileName = filePath.getFileName().toString();
    return FileMetadata.builder()
        .originalName(fileName)
        .storedName(fileName)
        .contentType(contentType)
        .size(Files.size(filePath))
        .fileType(fileType)
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 파일 삭제
  // ─────────────────────────────────────────────────────────────────────────────

  public void deleteAllAttachments(Collection<FileMetadata> files) {
    if (files == null || files.isEmpty()) return;

    Path uploadDir = fileConfig.getUploadDir();

    for (FileMetadata metadata : files) {
      if (metadata.getStoredName() == null) continue;
      try {
        Files.deleteIfExists(uploadDir.resolve(metadata.getStoredName()));
      } catch (IOException e) {
        throw new RuntimeException("첨부 파일 삭제 실패: " + metadata.getStoredName(), e);
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private
  // ─────────────────────────────────────────────────────────────────────────────

  private String buildStoredName(String originalName) {
    String ext = (originalName != null && originalName.contains("."))
        ? originalName.substring(originalName.lastIndexOf("."))
        : "";
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
  }

  private void transferFile(MultipartFile file, Path dest) {
    try {
      file.transferTo(dest);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + dest.toAbsolutePath(), e);
    }
  }

  private Path resolveBackupDir() {
    Path backupDir = fileConfig.getBackupDir();
    try {
      Files.createDirectories(backupDir);
    } catch (IOException e) {
      throw new RuntimeException("백업 디렉토리 생성 실패: " + backupDir.toAbsolutePath(), e);
    }
    return backupDir;
  }
}