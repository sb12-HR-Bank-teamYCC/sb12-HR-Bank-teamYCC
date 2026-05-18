package com.codeit.hrbank.common.exception;

import com.codeit.hrbank.dto.error.ErrorResponse;
import com.codeit.hrbank.dto.error.HrBankException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HrBankException.class)
  public ResponseEntity<ErrorResponse> handleHrBankException(HrBankException ex) {
    ErrorCode code = ex.getErrorCode();
    return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, ex.getMessage()));
  }
}