package com.codeit.hrbank.common.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class FileConfig {

    // 저장 경로는 application.yml에서 주입
    @Value("${hr-bank.storage.local.root-path}")
    private String storagePath;
    private Path uploadDir;

    @PostConstruct
    public void init() {

        uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();

        if (Files.notExists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadDir.toAbsolutePath(), e);
            }
        }
    }
}
