package com.codeit.hrbank.dto.error;

import com.codeit.hrbank.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class HrBankException extends RuntimeException {
  private final ErrorCode errorCode;

  public HrBankException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public HrBankException(ErrorCode errorCode, String details) {
    super(details);
    this.errorCode = errorCode;
  }
}
