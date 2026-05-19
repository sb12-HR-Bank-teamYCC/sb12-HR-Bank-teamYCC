package com.codeit.hrbank.dto.dataBackup;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DataBackupDto {
  private UUID id;
  private String worker;
  private Instant startedAt;
  private Instant endedAt;
  private String status;
  private UUID fileId;
}
