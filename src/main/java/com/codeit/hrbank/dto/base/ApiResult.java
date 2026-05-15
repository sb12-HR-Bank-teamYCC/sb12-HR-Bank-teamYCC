package com.codeit.hrbank.dto.base;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 응답 결과를 일정한 포맷으로 처리하기 위한 공통 응답 설계!
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 값은 직렬화 제외!
public class ApiResult<T> {
    private boolean success; // 응답의 성공 여부!
    private String message; // 응답 메세지

    private T data; // 실제 data 영역
    private ApiError error; // 에러메세지
    private Instant timestamp; // 응답 시간

    // 공통 API를 생성하기 위한 static 메소드
    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return ApiResult.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResult<T> ok(String message) {
        return ApiResult.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return ApiResult.<T>builder()
                .success(false)
                .error(new ApiError(code, message))
                .timestamp(Instant.now())
                .build();
    }
}
