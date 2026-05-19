package com.codeit.hrbank.mapper;

import com.codeit.hrbank.dto.employee.EmployeeCreateRequest;
import com.codeit.hrbank.dto.employee.EmployeeDto;
import com.codeit.hrbank.entity.Department;
import com.codeit.hrbank.entity.FileMetadata;
import com.codeit.hrbank.entity.employee.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "position", source = "request.position")
    @Mapping(target = "hireDate", source = "request.hireDate")

    @Mapping(target = "employeeNumber", source = "employeeNumber")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "profileImage", source = "profileImage")

    @Mapping(target = "status", constant = "ACTIVE")
    Employee toEntity(
            EmployeeCreateRequest request,
            String employeeNumber,
            Department department,
            FileMetadata profileImage
    );

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "profileImageId", source = "profileImage.id")
    EmployeeDto toDto(Employee employee);
}
