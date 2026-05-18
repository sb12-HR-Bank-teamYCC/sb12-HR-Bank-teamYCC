package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.employee.Employee;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmployeeNumber(String employeeNumber);
}
