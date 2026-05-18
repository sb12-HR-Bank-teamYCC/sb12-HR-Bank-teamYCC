package com.codeit.hrbank.entity.backupStatus;


import com.codeit.hrbank.entity.FileMetadata;
import com.codeit.hrbank.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "data_backups")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DataBackup extends BaseUpdatableEntity {

  @Column(name = "worker", length = 45)
  private String worker;

  @CreatedDate
  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  private BackupStatus status;

  //@OneToOne(fetch = FetchType.LAZY)
  //@JoinColumn(name = "file_id")
  //private FileMetadata fileMetadata;

  @Column(name = "file_id")
  private UUID fileMetadata;
}
