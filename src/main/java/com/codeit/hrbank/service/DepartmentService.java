package com.codeit.hrbank.service;

import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.department.DepartmentCreateRequest;
import com.codeit.hrbank.dto.department.DepartmentResponse;
import com.codeit.hrbank.dto.department.DepartmentSearchRequest;
import com.codeit.hrbank.dto.department.DepartmentSliceResponse;
import com.codeit.hrbank.dto.department.DepartmentUpdateRequest;
import com.codeit.hrbank.dto.department.SortDirection;
import com.codeit.hrbank.dto.department.SortField;
import com.codeit.hrbank.dto.error.HrBankException;
import com.codeit.hrbank.entity.Department;
import com.codeit.hrbank.repository.DepartmentRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  // 부서 등록
  @Transactional
  public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
    if (departmentRepository.existsByName(request.getName())) {
      throw new HrBankException(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
    }

    Department department = Department.builder()
        .name(request.getName())
        .description(request.getDescription())
        .establishedDate(request.getEstablishedDate())
        .build();

    Department saved = departmentRepository.save(department);
    return toResponse(saved, 0);
  }

  // 단건 조회
  @Transactional(readOnly = true)
  public DepartmentResponse getDepartment(UUID id) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new HrBankException(ErrorCode.DEPARTMENT_NOT_FOUND));

    int employeeCount = (int) departmentRepository.countEmployeesByDepartmentId(id);

    return toResponse(department, employeeCount);
  }

  // 부서 수정
  @Transactional
  public DepartmentResponse updateDepartment(UUID id, DepartmentUpdateRequest request) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new HrBankException(ErrorCode.DEPARTMENT_NOT_FOUND));

    if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
      throw new HrBankException(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
    }

    department.update(request.getName(), request.getDescription(), request.getEstablishedDate());
    Department updated = departmentRepository.save(department);
    int employeeCount = (int) departmentRepository.countEmployeesByDepartmentId(id);

    return toResponse(updated, employeeCount);
  }

  // 부서 삭제
  @Transactional
  public void deleteDepartment(UUID id) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new HrBankException(ErrorCode.DEPARTMENT_NOT_FOUND));

    long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
    if (employeeCount > 0) {
      throw new HrBankException(ErrorCode.DEPARTMENT_HAS_EMPLOYEES);
    }
    departmentRepository.delete(department);
  }

  // 부서 목록 조회 (검색, 정렬, 커서 기반 페이지네이션)
  @Transactional(readOnly = true)
  public DepartmentSliceResponse getDepartments(DepartmentSearchRequest request) {

    String keyword = request.getNameOrDescription();

    List<Department> departments;

    if (keyword == null || keyword.isBlank()) {
      departments = departmentRepository.findAll();
    } else {
      departments = departmentRepository.searchDepartments(keyword);
    }

    List<Department> filtered = departments.stream()
        .filter(d -> request.getIdAfter() == null
            || d.getId().compareTo(request.getIdAfter()) > 0)
        .sorted((d1, d2) -> {
          if (request.getSortFieldEnum() == SortField.NAME) {
            return request.getSortDirectionEnum() == SortDirection.ASC
                ? d1.getName().compareTo(d2.getName())
                : d2.getName().compareTo(d1.getName());
          }

          return request.getSortDirectionEnum() == SortDirection.ASC
              ? d1.getEstablishedDate().compareTo(d2.getEstablishedDate())
              : d2.getEstablishedDate().compareTo(d1.getEstablishedDate());
        })
        .collect(Collectors.toList());

    boolean hasNext = filtered.size() > request.getSize();

    List<Department> paged = filtered.stream()
        .limit(request.getSize())
        .collect(Collectors.toList());

    UUID nextCursor = paged.isEmpty()
        ? null
        : paged.get(paged.size() - 1).getId();

    return DepartmentSliceResponse.builder()
        .content(paged.stream()
            .map(d -> toResponse(d,
                (int) departmentRepository.countEmployeesByDepartmentId(d.getId())))
            .collect(Collectors.toList()))
        .nextCursor(nextCursor != null ? nextCursor.toString() : null)
        .nextIdAfter(nextCursor)
        .size(request.getSize())
        .totalElements(filtered.size())
        .hasNext(hasNext)
        .sortField(request.getSortField())
        .sortDirection(request.getSortDirection())
        .build();
  }

  private DepartmentResponse toResponse(Department department, int employeeCount) {
    return new DepartmentResponse(
        department.getId(),
        department.getName(),
        department.getDescription(),
        department.getEstablishedDate(),
        employeeCount
    );
  }
}