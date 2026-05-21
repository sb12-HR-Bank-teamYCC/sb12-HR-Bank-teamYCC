package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.Department;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, UUID id);

  @Query("""
         SELECT COUNT(e)
         FROM Employee e
         JOIN e.department d
         WHERE d.id = :departmentId
         """)
  long countEmployeesByDepartmentId(@Param("departmentId") UUID departmentId);

  @Query("""
       SELECT d
       FROM Department d
       WHERE (:keyword IS NULL
              OR d.name LIKE CONCAT('%', :keyword, '%')
              OR d.description LIKE CONCAT('%', :keyword, '%'))
       """)
  List<Department> searchDepartments(@Param("keyword") String keyword);

  @Query("""
         SELECT d
         FROM Department d
         WHERE (:cursor IS NULL OR d.id > :cursor)
         ORDER BY d.id ASC
         """)
  List<Department> findDepartmentsAfterCursor(@Param("cursor") UUID cursor);

  @Query("SELECT d FROM Department d ORDER BY d.name ASC")
  List<Department> findAllOrderByNameAsc();

  @Query("SELECT d FROM Department d ORDER BY d.establishedDate ASC")
  List<Department> findAllOrderByEstablishedDateAsc();

  @Query("SELECT COUNT(d) FROM Department d")
  long countAllDepartments();

}
