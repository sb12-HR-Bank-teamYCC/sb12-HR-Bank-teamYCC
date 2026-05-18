package com.codeit.hrbank;

import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.error.HrBankException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HrBankApplicationTests {

  @Test
  void contextLoads() {
  }

  public void ExceptionHandlerTest() {
    //throw new HrBankException(ErrorCode.INTERNAL_SERVER_ERROR, "테스트를 위한 예제");
    throw new HrBankException(ErrorCode.NOT_FOUND, "테스트를 위한 예제");
  }
}
