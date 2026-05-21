package com.codeit.hrbank.mapper;

import com.codeit.hrbank.dto.dataBackup.DataBackupDto;
import com.codeit.hrbank.entity.backupStatus.BackupStatus;
import com.codeit.hrbank.entity.backupStatus.DataBackup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DataBackupMapper {

  @Mapping(source = "status", target = "status")
  @Mapping(source = "fileMetadata.id", target = "fileId")
  DataBackupDto toDto(DataBackup entity);

  default String map(BackupStatus status) {
    return status.name();
  }
}