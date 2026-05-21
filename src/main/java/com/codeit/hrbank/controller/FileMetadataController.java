package com.codeit.hrbank.controller;

import com.codeit.hrbank.common.config.FileConfig;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.file.FileTypeConst;
import com.codeit.hrbank.service.FileMetadataService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileMetadataController {

  private final FileMetadataService fileMetadataService;
  private final FileConfig fileConfig;

  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {

    FileMetadata file = fileMetadataService.findById(id);

    Path filePath;

    switch (file.getFileType()) {

      case FileTypeConst.PROFILE_IMAGE ->

          filePath = fileConfig.getImageDir()
              .resolve(file.getStoredName())
              .normalize();

      case FileTypeConst.BACKUP,
           FileTypeConst.BACKUP_LOG ->

          filePath = fileConfig.getBackupDir()
              .resolve(file.getStoredName())
              .normalize();

      default -> {
        return ResponseEntity.badRequest().build();
      }
    }


    Resource resource;
    try {
      resource = new UrlResource(filePath.toUri());
      if (!resource.exists()) {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }

    // 파일명 인코딩 (URLEncoder 처리)
    String encodedFileName = UriUtils.encode(file.getOriginalName(), StandardCharsets.UTF_8);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSize()))
        .body(resource);
  }
}
