package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.changeLog.ChangeLog;
import com.codeit.hrbank.repository.querydsl.ChangeLogQueryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeLogRepository extends JpaRepository<ChangeLog, UUID>, ChangeLogQueryRepository {
}
