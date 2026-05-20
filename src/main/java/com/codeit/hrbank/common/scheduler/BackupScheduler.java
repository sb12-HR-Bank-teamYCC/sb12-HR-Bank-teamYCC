package com.codeit.hrbank.common.scheduler;

import com.codeit.hrbank.service.dataBackup.DataBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {
  private final DataBackupService dataBackupService;

  @Scheduled(cron = "${hr-bank.backup.schedule-cron}")
  public void runBackup() {
    log.info("[DataBackupScheduler] 배치 백업 시작");
    try {
      dataBackupService.backup("system");
      log.info("[DataBackupScheduler] 배치 백업 완료");
    } catch (Exception e) {
      // 스케줄러가 예외로 인해 중단되지 않도록 catch — 실패 상태는 서비스 내부에서 저장
      log.error("[DataBackupScheduler] 배치 백업 중 예외 발생", e);
    }
  }
}
