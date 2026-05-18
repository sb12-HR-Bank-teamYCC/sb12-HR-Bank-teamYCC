package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.FileMetadata;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

}
