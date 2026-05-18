package com.codeit.hrbank.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeCreateRequest(

        @NotBlank(message = "직원 이름은 필수입니다.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "부서 ID는 필수입니다.")
        UUID departmentId,

        @NotBlank(message = "직함은 필수입니다.")
        String position,

        @NotNull(message = "입사일은 필수입니다.")
        LocalDate hireDate,

        String memo
) {
}
