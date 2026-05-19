package com.codeit.hrbank.dto.error;

import com.codeit.hrbank.common.exception.ErrorCode;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private String timestamp;
  private int status;
  private String message;
  private String details;

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        Instant.now().toString(),
        errorCode.getStatus().value(),
        errorCode.getMessage(),
        null
    );
  }

  public static ErrorResponse of(ErrorCode errorCode, String details) {
    return new ErrorResponse(
        Instant.now().toString(),
        errorCode.getStatus().value(),
        errorCode.getMessage(),
        details
    );
  }
}