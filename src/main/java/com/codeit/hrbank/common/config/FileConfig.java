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
    private Path backupDir;
    private Path imageDir;

    @PostConstruct
    public void init() {
        uploadDir = createDirectory("uploads");
        backupDir = createDirectory("uploads/backups");
        imageDir = createDirectory("uploads/images");
    }

    private Path createDirectory(String path) {
        Path dir = Paths.get(System.getProperty("user.dir"), path);

        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(
                    "디렉토리 생성 실패: " + dir.toAbsolutePath(),
                    e
                );
            }
        }

        return dir;
    }
}