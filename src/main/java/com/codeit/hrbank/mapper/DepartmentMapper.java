package com.codeit.hrbank.mapper;

import com.codeit.hrbank.dto.department.DepartmentCreateRequest;
import com.codeit.hrbank.dto.department.DepartmentDto;
import com.codeit.hrbank.dto.department.DepartmentResponse;
import com.codeit.hrbank.dto.department.DepartmentUpdateRequest;
import com.codeit.hrbank.entity.Department;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  Department toEntity(DepartmentCreateRequest request);
  Department toEntity(DepartmentUpdateRequest request);

  DepartmentDto toDto(Department department, int employeeCount);
  DepartmentResponse toResponse(Department department, int employeeCount);

  @Mapping(target = "employeeCount", constant = "0")
  DepartmentResponse toResponse(Department department);
}