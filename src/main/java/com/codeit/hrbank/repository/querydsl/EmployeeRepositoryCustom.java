package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.entity.employee.EmployeeStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeRepositoryCustom {

  List<Employee> findAllWithFilters(
      String nameOrEmail, String employeeNumber, String departmentName,
      String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status, String cursorValue, UUID idAfter,
      int limit, String sortField, String sortDirection
  );

  long countWithFilters(
      String nameOrEmail, String employeeNumber, String departmentName,
      String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status
  );
}