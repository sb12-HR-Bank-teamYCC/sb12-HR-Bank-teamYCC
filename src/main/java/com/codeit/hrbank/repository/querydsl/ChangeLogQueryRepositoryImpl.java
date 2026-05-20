package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.changeLog.ChangeLog;
import com.codeit.hrbank.entity.changeLog.ChangeType;
import com.codeit.hrbank.entity.changeLog.QChangeLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChangeLogQueryRepositoryImpl implements ChangeLogQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QChangeLog cl = QChangeLog.changeLog;

    @Override
    public boolean existsByAtAfter(Instant lastEndedAt) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(cl)
                .where(cl.at.after(lastEndedAt))
                .fetchFirst();
        return fetchOne != null;
    }

    @Override
    public List<ChangeLog> findAllWithFilters(
            String employeeNumber,
            ChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo,
            UUID idAfter,
            String cursor,
            int limit,
            String sortField,
            String sortDirection
    ) {
        BooleanBuilder where = buildWhereClause(employeeNumber, type, memo, ipAddress, atFrom, atTo);

        if (cursor != null && !cursor.isBlank()) {
            applyCursorCondition(where, cursor, sortField, sortDirection);
        } else if (idAfter != null) {
            boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
            where.and(isAsc ? cl.id.gt(idAfter) : cl.id.lt(idAfter));
        }

        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
        OrderSpecifier<?> primaryOrder = resolveOrder(sortField, isAsc);
        OrderSpecifier<?> secondaryOrder = isAsc ? cl.id.asc() : cl.id.desc();

        return queryFactory
                .selectFrom(cl)
                .where(where)
                .orderBy(primaryOrder, secondaryOrder)
                .limit(limit)
                .fetch();
    }

    @Override
    public long countWithFilters(
            String employeeNumber,
            ChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo
    ) {
        BooleanBuilder where = buildWhereClause(employeeNumber, type, memo, ipAddress, atFrom, atTo);
        Long count = queryFactory
                .select(cl.id.count())
                .from(cl)
                .where(where)
                .fetchOne();
        return count == null ? 0L : count;
    }

    // ── 공통 WHERE 빌더 ──────────────────────────────────────────────────────────

    private BooleanBuilder buildWhereClause(
            String employeeNumber, ChangeType type, String memo,
            String ipAddress, Instant atFrom, Instant atTo
    ) {
        BooleanBuilder where = new BooleanBuilder();

        if (employeeNumber != null && !employeeNumber.isBlank()) {
            where.and(cl.employeeNumber.containsIgnoreCase(employeeNumber));
        }
        if (type != null) {
            where.and(cl.type.eq(type));
        }
        if (memo != null && !memo.isBlank()) {
            where.and(cl.memo.containsIgnoreCase(memo));
        }
        if (ipAddress != null && !ipAddress.isBlank()) {
            where.and(cl.ipAddress.containsIgnoreCase(ipAddress));
        }
        if (atFrom != null) {
            where.and(cl.at.goe(atFrom));
        }
        if (atTo != null) {
            where.and(cl.at.loe(atTo));
        }
        return where;
    }

    // ── 커서 조건 ────────────────────────────────────────────────────────────────

    // 커서 포맷: Base64(sortValue + ":" + uuid)
    //   at       → "2024-01-01T00:00:00Z:uuid"
    //   ipAddress → "192.168.0.1:uuid"
    private void applyCursorCondition(
            BooleanBuilder where, String cursor, String sortField, String sortDirection
    ) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            int splitIdx = decoded.lastIndexOf(':');
            if (splitIdx < 0) return;

            String sortValue = decoded.substring(0, splitIdx);
            UUID cursorId = UUID.fromString(decoded.substring(splitIdx + 1));
            boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

            String field = (sortField == null) ? "at" : sortField;

            switch (field) {
                case "ipAddress" -> {
                    if (isAsc) {
                        where.and(
                                cl.ipAddress.gt(sortValue)
                                        .or(cl.ipAddress.eq(sortValue).and(cl.id.gt(cursorId)))
                        );
                    } else {
                        where.and(
                                cl.ipAddress.lt(sortValue)
                                        .or(cl.ipAddress.eq(sortValue).and(cl.id.lt(cursorId)))
                        );
                    }
                }
                default -> { // at
                    Instant cursorAt = Instant.parse(sortValue);
                    if (isAsc) {
                        where.and(
                                cl.at.gt(cursorAt)
                                        .or(cl.at.eq(cursorAt).and(cl.id.gt(cursorId)))
                        );
                    } else {
                        where.and(
                                cl.at.lt(cursorAt)
                                        .or(cl.at.eq(cursorAt).and(cl.id.lt(cursorId)))
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[ChangeLogQueryRepository] 커서 파싱 실패, 무시합니다. cursor={}", cursor, e);
        }
    }

    // ── 정렬 ─────────────────────────────────────────────────────────────────────

    private OrderSpecifier<?> resolveOrder(String sortField, boolean isAsc) {
        String field = (sortField == null || sortField.isBlank()) ? "at" : sortField;
        return switch (field) {
            case "ipAddress" -> isAsc ? cl.ipAddress.asc() : cl.ipAddress.desc();
            default -> isAsc ? cl.at.asc() : cl.at.desc(); // at
        };
    }
}
