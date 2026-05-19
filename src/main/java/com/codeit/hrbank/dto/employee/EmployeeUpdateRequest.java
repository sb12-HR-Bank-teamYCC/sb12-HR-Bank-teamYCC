package com.codeit.hrbank.dto.employee;

import com.codeit.hrbank.entity.employee.EmployeeStatus;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeUpdateRequest(

        String name,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        UUID departmentId,

        String position,

        LocalDate hireDate,

        EmployeeStatus status,

        String memo
) {

}
