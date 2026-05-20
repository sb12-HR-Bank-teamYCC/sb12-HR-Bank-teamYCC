package com.codeit.hrbank.service;

import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.employee.Employee;

import java.util.UUID;

public record EmployeeSnapshot(
        UUID id,
        String employeeNumber,
        String name,
        String email,
        String departmentName,
        String position,
        String hireDate,
        String status,
        String profileImageId
) {

    public static EmployeeSnapshot from(Employee employee) {
        FileMetadata profileImage = employee.getProfileImage();

        return new EmployeeSnapshot(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment().getName(),
                employee.getPosition(),
                employee.getHireDate() == null ? null : employee.getHireDate().toString(),
                employee.getStatus() == null ? null : employee.getStatus().name(),
                profileImage == null ? null : profileImage.getId().toString()
        );
    }
}
