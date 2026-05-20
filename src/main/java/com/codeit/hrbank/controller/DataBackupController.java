package com.codeit.hrbank.controller;

import com.codeit.hrbank.dto.dataBackup.DataBackupDto;
import com.codeit.hrbank.dto.page.CursorPageResponse;
import com.codeit.hrbank.service.dataBackup.DataBackupService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
public class DataBackupController {

  private final DataBackupService dataBackupService;

  // ─────────────────────────────────────────────────────────────────────────────
  // POST /api/backups - 데이터 백업 생성
  // ─────────────────────────────────────────────────────────────────────────────
  @PostMapping
  public ResponseEntity<DataBackupDto> createBackup(HttpServletRequest request) {
    String worker = extractIp(request);
    DataBackupDto result = dataBackupService.backup(worker);
    return ResponseEntity.ok(result);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // GET /api/backups - 데이터 백업 목록 조회
  // ─────────────────────────────────────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<CursorPageResponse<DataBackupDto>> getAllBackups(
      @RequestParam(required = false) String worker,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startedAtTo,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "startedAt") String sortField,
      @RequestParam(defaultValue = "DESC") String sortDirection
  ) {
    CursorPageResponse<DataBackupDto> response = dataBackupService.getBackups(
        worker, status, startedAtFrom, startedAtTo,
        idAfter, cursor, size, sortField, sortDirection
    );
    return ResponseEntity.ok(response);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // GET /api/backups/latest - 최근 백업 정보 조회
  // ─────────────────────────────────────────────────────────────────────────────
  @GetMapping("/latest")
  public ResponseEntity<DataBackupDto> getLatestBackup(
      @Parameter(description = "백업 상태 (COMPLETED, FAILED, IN_PROGRESS, 기본값: COMPLETED)")
      @RequestParam(defaultValue = "COMPLETED") String status
  ) {
    DataBackupDto result = dataBackupService.getLatestBackup(status);
    return ResponseEntity.ok(result);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private: 요청자 IP 추출
  // 프록시/로드밸런서 환경(X-Forwarded-For)도 고려
  // ─────────────────────────────────────────────────────────────────────────────
  private String extractIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim(); // 첫 번째 IP가 실제 클라이언트
    }
    return request.getRemoteAddr();
  }
}