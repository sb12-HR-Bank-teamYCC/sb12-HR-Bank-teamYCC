package com.codeit.hrbank.service;

import com.codeit.hrbank.common.file.FileStorage;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.repository.FileMetadataRepository;
import java.io.IOException;
import java.nio.file.Path;
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

  // ─────────────────────────────────────────────────────────────────────────────
  // 조회
  // ─────────────────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public FileMetadata findById(UUID id) {
    return fileMetadataRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 파일을 찾을 수 없습니다. ID: " + id));
  }

  @Transactional(readOnly = true)
  public FileMetadata findByStoredName(String storedName) {
    return fileMetadataRepository.findByStoredName(storedName)
        .orElseThrow(() -> new IllegalArgumentException("파일 이름을 찾을 수 없습니다."));
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 저장
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 프로필 이미지를 uploads/images/에 저장하고 DB에 persist합니다.
   */
  @Transactional
  public FileMetadata saveProfileImage(MultipartFile file) {
    if (file == null || file.isEmpty()) return null;
    FileMetadata metadata = fileStorage.saveProfileImage(file);
    return fileMetadataRepository.save(metadata);
  }

  /**
   * 일반 첨부 파일 목록을 uploads/에 저장하고 DB에 persist합니다.
   */
  @Transactional
  public List<FileMetadata> save(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) return List.of();
    return files.stream()
        .filter(mf -> mf != null && !mf.isEmpty())
        .map(mf -> fileMetadataRepository.save(fileStorage.saveAttachFile(mf)))
        .toList();
  }

  /**
   * 이미 디스크에 존재하는 파일(백업 CSV, 에러 로그 등)의 메타데이터를 DB에 persist합니다.
   */
  @Transactional
  public FileMetadata saveFromPath(Path filePath, String contentType, String fileType)
      throws IOException {
    FileMetadata metadata = fileStorage.metadataFromPath(filePath, contentType, fileType);
    return fileMetadataRepository.save(metadata);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // 삭제
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * 파일을 디스크와 DB에서 모두 삭제합니다.
   */
  @Transactional
  public void delete(Collection<FileMetadata> attachments) {
    if (attachments == null || attachments.isEmpty()) return;
    fileStorage.deleteAllAttachments(attachments);
    fileMetadataRepository.deleteAll(attachments);
  }
}