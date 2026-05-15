package com.codeit.hrbank.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileConfig fileConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")           // 모든 경로에 대해
                .allowedOriginPatterns("*")
                .allowedMethods("*")         // 모든 HTTP 메소드 허용
                .allowedHeaders("*")         // 모든 요청 헤더 허용
                .allowCredentials(true)      // 인증정보 포함
                .maxAge(3600);               // Preflight 캐시 1시간
    }

    // 업로드 폴더를 /files/** URL로 정적 리소스 노출
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:///" + fileConfig.getUploadDir().toAbsolutePath() + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(uploadPath);
    }
}
