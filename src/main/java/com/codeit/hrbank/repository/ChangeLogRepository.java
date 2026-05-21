package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.changeLog.ChangeLog;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.codeit.hrbank.repository.querydsl.ChangeLogQueryRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, UUID>, ChangeLogQueryRepository {

    @EntityGraph(attributePaths = "diffs")
    Optional<ChangeLog> findWithDiffsById(UUID id);

    @Query("""
        SELECT COUNT(c)
        FROM ChangeLog c
        WHERE c.at >= :fromDate
          AND c.at <= :toDate
    """)
    long countByAtBetween(@Param("fromDate") Instant fromDate,
                          @Param("toDate") Instant toDate);
}
