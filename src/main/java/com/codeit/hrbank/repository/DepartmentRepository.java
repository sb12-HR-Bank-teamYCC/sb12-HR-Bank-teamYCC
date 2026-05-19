package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.Department;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

}
