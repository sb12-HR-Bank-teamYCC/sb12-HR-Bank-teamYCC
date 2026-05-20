package com.codeit.hrbank.common.file;

import com.codeit.hrbank.common.config.FileConfig;
import com.codeit.hrbank.entity.file.FileMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileStorage {

  private final FileConfig fileConfig;

  // 파일 저장 (실제 파일은 로컬 디스크에 저장, DB에 저장할 메타데이터로 반환)
  public FileMetadata saveAttachFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
    }

    Path uploadDir = fileConfig.getUploadDir();

    // 원본 파일명, 확장자 추출
    String originalName = file.getOriginalFilename();
    String ext = (originalName != null && originalName.contains("."))
        ? originalName.substring(originalName.lastIndexOf("."))
        : "";

    // 날짜 + UUID 기반 storedName
    String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")
        .format(LocalDateTime.now());
    String storedName = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

    Path dest = uploadDir.resolve(storedName);

    try {
      file.transferTo(dest);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + dest.toAbsolutePath(), e);
    }

    return FileMetadata.builder()
        .originalName(originalName)
        .storedName(storedName)
        .contentType(file.getContentType())
        .size(file.getSize())
        .build();
  }

  // 파일 삭제
  public void deleteAllAttachments(Collection<FileMetadata> files) {
    if (files == null || files.isEmpty()) return;

    Path uploadDir = fileConfig.getUploadDir();

    for (FileMetadata metadata : files) {
      if (metadata.getStoredName() == null) continue;

      try {
        Files.deleteIfExists(uploadDir.resolve(metadata.getStoredName()));
      } catch (IOException e) {
        throw new RuntimeException("첨부 파일 삭제 실패: " + metadata.getStoredName(), e);
      }
    }
  }
}