package com.codeit.hrbank.dto.employee;

import com.codeit.hrbank.entity.FileMetadata;
import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.entity.employee.EmployeeStatus;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeDto(
        UUID id,
        String name,
        String email,
        String employeeNumber,
        UUID departmentId,
        String departmentName,
        String position,
        LocalDate hireDate,
        EmployeeStatus status,
        UUID profileImageId
) {

    public static EmployeeDto from(Employee employee) {
        FileMetadata profileImage = employee.getProfileImage();
        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getEmployeeNumber(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName(),
                employee.getPosition(),
                employee.getHireDate(),
                employee.getStatus(),
                profileImage == null ? null : profileImage.getId()
        );
    }
}
