package com.codeit.hrbank.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 400 Bad Request
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다."),
  INVALID_GROUP_BY(HttpStatus.BAD_REQUEST, "지원하지 않는 그룹화 기준입니다."),
  INVALID_TIME_UNIT(HttpStatus.BAD_REQUEST, "지원하지 않는 시간 단위입니다."),
  INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상태값입니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
  DUPLICATE_DEPARTMENT_NAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 부서 이름입니다."),
  DEPARTMENT_HAS_EMPLOYEES(HttpStatus.BAD_REQUEST, "소속 직원이 있는 부서는 삭제할 수 없습니다."),

  // 404 Not Found
  NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없습니다."),
  EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."),
  DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부서를 찾을 수 없습니다."),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
  CHANGE_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "수정 이력을 찾을 수 없습니다."),

  // 409 Conflict
  BACKUP_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 백업이 있습니다."),

  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String message;
}