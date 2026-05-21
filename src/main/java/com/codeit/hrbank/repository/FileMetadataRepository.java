package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.file.FileMetadata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
  Optional<FileMetadata> findByStoredName(String storedName);
}
