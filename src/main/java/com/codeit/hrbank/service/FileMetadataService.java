package com.codeit.hrbank.service;

import com.codeit.hrbank.common.file.FileStorage;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.repository.FileMetadataRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

  private final FileMetadataRepository fileMetadataRepository;
  private final FileStorage fileStorage;

  // 파일 조회(ID)
  @Transactional(readOnly = true)
  public FileMetadata findById(UUID id) {
    return fileMetadataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 파일을 찾을 수 없습니다. ID: " + id));
  }

  // 파일 조회(storedName)
  @Transactional(readOnly = true)
  public FileMetadata findByStoredName(String storedName) {
    return fileMetadataRepository.findByStoredName(storedName)
        .orElseThrow(() -> new IllegalArgumentException("파일 이름을 찾을 수 없습니다."));
  }

  // 파일 저장 (디스크 + DB저장)
  @Transactional
  public List<FileMetadata> save(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) return List.of();
    return files.stream()
        .filter(mf -> mf != null && !mf.isEmpty())
        .map(mf -> fileMetadataRepository.save(fileStorage.saveAttachFile(mf)))
        .toList();
  }

  // 파일 삭제 (디스크 + DB 삭제)
  @Transactional
  public void delete(Collection<FileMetadata> attachments) {
    if (attachments == null || attachments.isEmpty()) return;

    fileStorage.deleteAllAttachments(attachments); // 디스크 삭제
    fileMetadataRepository.deleteAll(attachments); // DB 삭제
  }
}