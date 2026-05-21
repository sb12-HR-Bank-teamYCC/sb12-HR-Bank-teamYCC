package com.codeit.hrbank.service;

import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.department.DepartmentCreateRequest;
import com.codeit.hrbank.dto.department.DepartmentResponse;
import com.codeit.hrbank.dto.department.DepartmentSearchRequest;
import com.codeit.hrbank.dto.department.DepartmentSliceResponse;
import com.codeit.hrbank.dto.department.DepartmentUpdateRequest;
import com.codeit.hrbank.dto.department.SortDirection;
import com.codeit.hrbank.dto.department.SortField;
import com.codeit.hrbank.dto.employee.EmployeeDto;
import com.codeit.hrbank.dto.error.HrBankException;
import com.codeit.hrbank.dto.page.CursorPageResponse;
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
      throw new IllegalArgumentException("이미 존재하는 부서 이름입니다");
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
      throw new IllegalArgumentException("이미 존재하는 부서 이름입니다");
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
      throw new IllegalStateException("소속된 직원이 있어 삭제할 수 없습니다");
    }
    departmentRepository.delete(department);
  }

  // 부서 목록 조회 (검색, 정렬, 커서 기반 페이지네이션)
  @Transactional(readOnly = true)
  public CursorPageResponse<DepartmentResponse> getDepartments(DepartmentSearchRequest request) {

    String keyword = request.getNameOrDescription();

    List<Department> departments;
    if (keyword == null || keyword.isBlank()) {
      departments = departmentRepository.findAll();
    } else {
      departments = departmentRepository.searchDepartments(keyword);
    }

    // 1. 먼저 정렬
    List<Department> sorted = departments.stream()
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

    // 2. idAfter 위치 기준으로 커서 적용
    int startIndex = 0;
    if (request.getIdAfter() != null) {
      for (int i = 0; i < sorted.size(); i++) {
        if (sorted.get(i).getId().equals(request.getIdAfter())) {
          startIndex = i + 1;
          break;
        }
      }
    }

    List<Department> afterCursor = sorted.subList(startIndex, sorted.size());

    boolean hasNext = afterCursor.size() > request.getSize();

    List<DepartmentResponse> paged = afterCursor.stream()
        .limit(request.getSize())
        .map(d -> toResponse(d,
            (int) departmentRepository.countEmployeesByDepartmentId(d.getId())))
        .collect(Collectors.toList());

    String nextCursor = paged.isEmpty()
        ? null
        : paged.get(paged.size() - 1).getId().toString();

    return new CursorPageResponse<>(
        paged,
        nextCursor,
        nextCursor,
        request.getSize(),
        sorted.size(),   // totalElements는 전체 기준
        hasNext
    );
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