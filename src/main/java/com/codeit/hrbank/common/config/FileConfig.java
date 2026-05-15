package com.codeit.hrbank.common.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class FileConfig {

    private Path uploadDir;

    @PostConstruct
    public void init() {
        // user.dir : 애플리케이션 실행 위치 기준 uploads 폴더
        uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
        if (Files.notExists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadDir.toAbsolutePath(), e);
            }
        }
    }
}
