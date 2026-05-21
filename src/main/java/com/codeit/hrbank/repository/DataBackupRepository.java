package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.backupStatus.DataBackup;
import com.codeit.hrbank.repository.querydsl.DataBackupQueryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataBackupRepository extends JpaRepository<DataBackup, UUID>, DataBackupQueryRepository {

}
