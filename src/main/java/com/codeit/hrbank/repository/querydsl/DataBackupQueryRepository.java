package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.backupStatus.BackupStatus;
import com.codeit.hrbank.entity.backupStatus.DataBackup;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataBackupQueryRepository {

  // 마지막 백업 시간(epoch millis) 이전의 가장 최근 완료 백업 조회
  // → STEP.1 백업 필요 여부 판단 시 활용
  Optional<DataBackup> findByLastBackupTime(long lastBackupTime);

  // 특정 상태의 가장 최근 백업 조회
  // → GET /api/backups/latest 에서 활용
  Optional<DataBackup> findLatestByStatus(BackupStatus status);

  // 필터 + 커서 페이지네이션 목록 조회
  // → GET /api/backups 에서 활용
  List<DataBackup> findAllWithFilters(
      String worker,
      BackupStatus status,
      Instant startedAtFrom,
      Instant startedAtTo,
      UUID idAfter,
      String cursor,
      int size,
      String sortField,
      String sortDirection
  );

  // 전체 건수 조회 (totalElements 계산용)
  long countWithFilters(
      String worker,
      BackupStatus status,
      Instant startedAtFrom,
      Instant startedAtTo
  );
}
