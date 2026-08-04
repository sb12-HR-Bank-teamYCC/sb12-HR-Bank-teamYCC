package com.codeit.hrbank.service.dataBackup;

import com.codeit.hrbank.entity.backupStatus.BackupStatus;
import com.codeit.hrbank.entity.backupStatus.DataBackup;
import com.codeit.hrbank.repository.DataBackupRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataBackupTxService {

  private final DataBackupRepository dataBackupRepository;

  @Transactional
  public DataBackup createInProgressBackup(String worker) {
    return dataBackupRepository.save(
        DataBackup.builder()
            .worker(worker)
            .startedAt(Instant.now())
            .status(BackupStatus.IN_PROGRESS)
            .build()
    );
  }
}
