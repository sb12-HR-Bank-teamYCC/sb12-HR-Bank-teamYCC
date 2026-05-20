package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.changeLog.ChangeLog;
import com.codeit.hrbank.entity.changeLog.ChangeType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChangeLogQueryRepository {

    boolean existsByAtAfter(Instant lastEndedAt);

    List<ChangeLog> findAllWithFilters(
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
    );

    long countWithFilters(
            String employeeNumber,
            ChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo
    );
}
