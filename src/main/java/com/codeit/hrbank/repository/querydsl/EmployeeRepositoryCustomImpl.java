package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.entity.employee.EmployeeStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

  private final EntityManager em;

  @Override
  public List<Employee> findAllWithFilters(
      String nameOrEmail, String employeeNumber, String departmentName,
      String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status, String cursorValue, UUID idAfter,
      int limit, String sortField, String sortDirection
  ) {
    Map<String, Object> params = new LinkedHashMap<>();
    StringBuilder jpql = new StringBuilder(
        "SELECT e FROM Employee e JOIN FETCH e.department d WHERE 1=1"
    );

    appendFilters(jpql, params, nameOrEmail, employeeNumber, departmentName,
        position, hireDateFrom, hireDateTo, status);

    if (cursorValue != null && idAfter != null) {
      appendCursorCondition(jpql, params, cursorValue, idAfter, sortField, sortDirection);
    } else if (idAfter != null) {
      boolean isAsc = !"desc".equalsIgnoreCase(sortDirection);
      jpql.append(isAsc ? " AND e.id > :idAfter" : " AND e.id < :idAfter");
      params.put("idAfter", idAfter);
    }

    String entityField = resolveEntityField(sortField);
    String dir = "desc".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
    jpql.append(" ORDER BY ").append(entityField).append(" ").append(dir)
        .append(", e.id ").append(dir);

    TypedQuery<Employee> query = em.createQuery(jpql.toString(), Employee.class);
    params.forEach(query::setParameter);
    query.setMaxResults(limit);

    return query.getResultList();
  }

  @Override
  public long countWithFilters(
      String nameOrEmail, String employeeNumber, String departmentName,
      String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status
  ) {
    Map<String, Object> params = new LinkedHashMap<>();
    StringBuilder jpql = new StringBuilder(
        "SELECT COUNT(e) FROM Employee e JOIN e.department d WHERE 1=1"
    );

    appendFilters(jpql, params, nameOrEmail, employeeNumber, departmentName,
        position, hireDateFrom, hireDateTo, status);

    TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
    params.forEach(query::setParameter);

    return query.getSingleResult();
  }

  // ── 공통 필터 조건 ───────────────────────────────────────────────────────────

  private void appendFilters(
      StringBuilder jpql, Map<String, Object> params,
      String nameOrEmail, String employeeNumber, String departmentName,
      String position, LocalDate hireDateFrom, LocalDate hireDateTo,
      EmployeeStatus status
  ) {
    if (nameOrEmail != null) {
      jpql.append(" AND (e.name LIKE :nameOrEmail OR e.email LIKE :nameOrEmail)");
      params.put("nameOrEmail", "%" + nameOrEmail + "%");
    }
    if (employeeNumber != null) {
      jpql.append(" AND e.employeeNumber LIKE :employeeNumber");
      params.put("employeeNumber", "%" + employeeNumber + "%");
    }
    if (departmentName != null) {
      jpql.append(" AND d.name LIKE :departmentName");
      params.put("departmentName", "%" + departmentName + "%");
    }
    if (position != null) {
      jpql.append(" AND e.position LIKE :position");
      params.put("position", "%" + position + "%");
    }
    if (hireDateFrom != null) {
      jpql.append(" AND e.hireDate >= :hireDateFrom");
      params.put("hireDateFrom", hireDateFrom);
    }
    if (hireDateTo != null) {
      jpql.append(" AND e.hireDate <= :hireDateTo");
      params.put("hireDateTo", hireDateTo);
    }
    if (status != null) {
      jpql.append(" AND e.status = :status");
      params.put("status", status);
    }
  }

  // ── 커서 조건 ────────────────────────────────────────────────────────────────

  private void appendCursorCondition(
      StringBuilder jpql, Map<String, Object> params,
      String cursorValue, UUID idAfter,
      String sortField, String sortDirection
  ) {
    String field = resolveEntityField(sortField);
    boolean isAsc = !"desc".equalsIgnoreCase(sortDirection);
    String op = isAsc ? ">" : "<";

    // (field > cursorValue) OR (field = cursorValue AND id > idAfter)
    jpql.append(" AND (")
        .append(field).append(" ").append(op).append(" :cursorValue")
        .append(" OR (")
        .append(field).append(" = :cursorValue")
        .append(" AND e.id ").append(op).append(" :idAfter")
        .append("))");

    params.put("cursorValue", parseCursorValue(cursorValue, sortField));
    params.put("idAfter", idAfter);
  }

  // ── 유틸 ─────────────────────────────────────────────────────────────────────

  private String resolveEntityField(String sortField) {
    return switch (sortField == null ? "name" : sortField.toLowerCase()) {
      case "employeenumber" -> "e.employeeNumber";
      case "hiredate"       -> "e.hireDate";
      default               -> "e.name";
    };
  }

  private Object parseCursorValue(String cursorValue, String sortField) {
    String decoded = new String(Base64.getDecoder().decode(cursorValue), java.nio.charset.StandardCharsets.UTF_8);
    if ("hireDate".equalsIgnoreCase(sortField)) {
      return LocalDate.parse(decoded);
    }
    return decoded; // name, employeeNumber
  }
}