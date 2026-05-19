package com.codeit.hrbank.repository;

import com.codeit.hrbank.entity.employee.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.codeit.hrbank.entity.employee.EmployeeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmployeeNumber(String employeeNumber);

    @Query("""
        SELECT COUNT(e)
        FROM Employee e
        WHERE (:status IS NULL OR e.status = :status)
          AND (:fromDate IS NULL OR e.hireDate >= :fromDate)
          AND (:toDate IS NULL OR e.hireDate <= :toDate)
    """)
    long countEmployees(
            @Param("status") EmployeeStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
        SELECT e.department.name, COUNT(e)
        FROM Employee e
        WHERE e.status = :status
        GROUP BY e.department.name
    """)
    List<Object[]> countByDepartment(@Param("status") EmployeeStatus status);

    @Query("""
        SELECT e.position, COUNT(e)
        FROM Employee e
        WHERE e.status = :status
        GROUP BY e.position
    """)
    List<Object[]> countByPosition(@Param("status") EmployeeStatus status);

    @Query("""
        SELECT e.hireDate
        FROM Employee e
        WHERE e.hireDate <= :to
    """)
    List<LocalDate> findHireDatesUntil(@Param("to") LocalDate to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM Employee e
        WHERE e.employeeNumber LIKE CONCAT(:prefix, '%')
        ORDER BY e.employeeNumber DESC
    """)
    List<Employee> findLatestByEmployeeNumberPrefixForUpdate(
            @Param("prefix") String prefix,
            Pageable pageable
    );

    /**
     * 데이터 백업용 청크 조회.
     * department를 JOIN FETCH 해서 N+1 없이 한 번에 가져옵니다.
     * countQuery를 별도로 지정해 페이징 COUNT 쿼리에서 JOIN FETCH 사용을 방지합니다.
     */
    @Query(
        value = "SELECT e FROM Employee e JOIN FETCH e.department ORDER BY e.id",
        countQuery = "SELECT COUNT(e) FROM Employee e"
    )
    Page<Employee> findAllWithDepartmentForBackup(Pageable pageable);
}
