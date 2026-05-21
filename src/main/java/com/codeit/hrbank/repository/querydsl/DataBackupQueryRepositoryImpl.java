package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.backupStatus.BackupStatus;
import com.codeit.hrbank.entity.backupStatus.DataBackup;
import com.codeit.hrbank.entity.backupStatus.QDataBackup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DataBackupQueryRepositoryImpl implements DataBackupQueryRepository {

  private final JPAQueryFactory queryFactory;

  // QueryDSL이 @Entity 스캔 후 자동 생성하는 QType 선언
  private static final QDataBackup db = QDataBackup.dataBackup;

  // ─────────────────────────────────────────────────────────────────────────────
  // 3. 특정 상태의 최근 백업 1건 조회
  //    - GET /api/backups/latest?status=COMPLETED (기본값)
  // ─────────────────────────────────────────────────────────────────────────────
  @Override
  public Optional<DataBackup> findLatestByStatus(BackupStatus status) {
    return Optional.ofNullable(
        queryFactory
            .selectFrom(db)
            .leftJoin(db.fileMetadata).fetchJoin()
            .where(db.status.eq(status))
            .orderBy(db.startedAt.desc())
            .fetchFirst()
    );
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 4. 필터 + 커서 기반 페이지네이션 목록 조회
  //    - GET /api/backups?worker=&status=&startedAtFrom=&startedAtTo=
  //                      &cursor=&idAfter=&size=10&sortField=startedAt&sortDirection=DESC
  //
  //    커서 포맷: Base64(sortValue + ":" + uuid)
  //      예) startedAt 기준 → Base64("2025-01-01T00:00:00Z:550e8400-...")
  //
  //    size+1 개 조회 후 hasNext 판단 (Slice 패턴)
  // ─────────────────────────────────────────────────────────────────────────────
  @Override
  public List<DataBackup> findAllWithFilters(
      String worker,
      BackupStatus status,
      Instant startedAtFrom,
      Instant startedAtTo,
      UUID idAfter,
      String cursor,
      int size,
      String sortField,
      String sortDirection
  ) {
    BooleanBuilder where = buildWhereClause(worker, status, startedAtFrom, startedAtTo);

    // 커서가 있으면 커서 조건 적용, 없으면 idAfter 단순 fallback
    if (cursor != null && !cursor.isBlank()) {
      applyCursorCondition(where, cursor, sortField, sortDirection);
    } else if (idAfter != null) {
      boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
      where.and(isAsc ? db.id.gt(idAfter) : db.id.lt(idAfter));
    }

    OrderSpecifier<?> primaryOrder = resolveOrder(sortField, sortDirection);
    // 동일 정렬값이 있을 때 ID로 보조 정렬 → 페이지 경계 안정화
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
    OrderSpecifier<?> secondaryOrder = isAsc ? db.id.asc() : db.id.desc();

    return queryFactory
        .selectFrom(db)
        .leftJoin(db.fileMetadata).fetchJoin()
        .where(where)
        .orderBy(primaryOrder, secondaryOrder)
        .limit(size + 1L) // +1 로 hasNext 판단 (서비스 레이어에서 처리)
        .fetch();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 5. 전체 건수 조회 (totalElements 응답용)
  // ─────────────────────────────────────────────────────────────────────────────
  @Override
  public long countWithFilters(
      String worker,
      BackupStatus status,
      Instant startedAtFrom,
      Instant startedAtTo
  ) {
    BooleanBuilder where = buildWhereClause(worker, status, startedAtFrom, startedAtTo);
    Long count = queryFactory
        .select(db.id.count())
        .from(db)
        .where(where)
        .fetchOne();
    return count == null ? 0L : count;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Private 헬퍼 메서드
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 공통 where 조건 빌더
   * - worker       : 부분 일치 (containsIgnoreCase)
   * - status       : 완전 일치
   * - startedAtFrom/To : 범위 조건
   */
  private BooleanBuilder buildWhereClause(
      String worker,
      BackupStatus status,
      Instant startedAtFrom,
      Instant startedAtTo
  ) {
    BooleanBuilder where = new BooleanBuilder();

    if (worker != null && !worker.isBlank()) {
      where.and(db.worker.containsIgnoreCase(worker));
    }
    if (status != null) {
      where.and(db.status.eq(status));
    }
    if (startedAtFrom != null) {
      where.and(db.startedAt.goe(startedAtFrom));
    }
    if (startedAtTo != null) {
      where.and(db.startedAt.loe(startedAtTo));
    }

    return where;
  }

  /**
   * 커서 디코딩 후 WHERE 조건 추가
   *
   * <p>커서 포맷: Base64Url( sortFieldValue + ":" + uuid )
   *   - startedAt / endedAt → ISO-8601 Instant 문자열
   *   - status             → enum name 문자열
   *
   * <p>복합 커서 조건 (Keyset Pagination):
   *   ASC  → (sortVal > cursorSortVal) OR (sortVal = cursorSortVal AND id > cursorId)
   *   DESC → (sortVal < cursorSortVal) OR (sortVal = cursorSortVal AND id < cursorId)
   */
  private void applyCursorCondition(
      BooleanBuilder where,
      String cursor,
      String sortField,
      String sortDirection
  ) {
    try {
      String decoded = new String(Base64.getUrlDecoder().decode(cursor));
      int splitIdx = decoded.lastIndexOf(':');
      if (splitIdx < 0) return;

      String sortValue = decoded.substring(0, splitIdx);
      UUID cursorId = UUID.fromString(decoded.substring(splitIdx + 1));
      boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

      String field = (sortField == null) ? "startedAt" : sortField;

      switch (field) {
        case "startedAt" -> {
          Instant cursorTime = Instant.parse(sortValue);
          if (isAsc) {
            where.and(
                db.startedAt.gt(cursorTime)
                    .or(db.startedAt.eq(cursorTime).and(db.id.gt(cursorId)))
            );
          } else {
            where.and(
                db.startedAt.lt(cursorTime)
                    .or(db.startedAt.eq(cursorTime).and(db.id.lt(cursorId)))
            );
          }
        }
        case "endedAt" -> {
          Instant cursorTime = Instant.parse(sortValue);
          if (isAsc) {
            where.and(
                db.endedAt.gt(cursorTime)
                    .or(db.endedAt.eq(cursorTime).and(db.id.gt(cursorId)))
            );
          } else {
            where.and(
                db.endedAt.lt(cursorTime)
                    .or(db.endedAt.eq(cursorTime).and(db.id.lt(cursorId)))
            );
          }
        }
        // status 정렬 시 커서는 id만 사용
        default -> where.and(isAsc ? db.id.gt(cursorId) : db.id.lt(cursorId));
      }

    } catch (Exception e) {
      // 커서 파싱 실패 → 조건 없이 첫 페이지부터 조회
      log.warn("[DataBackupQueryRepository] 커서 파싱 실패, 무시합니다. cursor={}", cursor, e);
    }
  }

  /**
   * 정렬 화이트리스트
   * 허용 필드: startedAt(기본), endedAt, status
   * 이 외 값은 기본(startedAt DESC)으로 fallback
   */
  private OrderSpecifier<?> resolveOrder(String sortField, String sortDirection) {
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
    String field = (sortField == null || sortField.isBlank()) ? "startedAt" : sortField;

    return switch (field) {
      case "endedAt" -> isAsc ? db.endedAt.asc() : db.endedAt.desc();
      case "status"  -> isAsc ? db.status.asc()  : db.status.desc();
      default        -> isAsc ? db.startedAt.asc() : db.startedAt.desc(); // startedAt
    };
  }
}
