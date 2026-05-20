package com.codeit.hrbank.controller;

import com.codeit.hrbank.dto.department.DepartmentCreateRequest;
import com.codeit.hrbank.dto.department.DepartmentResponse;
import com.codeit.hrbank.dto.department.DepartmentSearchRequest;
import com.codeit.hrbank.dto.department.DepartmentSliceResponse;
import com.codeit.hrbank.dto.department.DepartmentUpdateRequest;
import com.codeit.hrbank.service.DepartmentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
  private final DepartmentService departmentService;

  // 부서 등록
  @PostMapping
  public ResponseEntity<DepartmentResponse> createDepartment(
      @RequestBody DepartmentCreateRequest request) {
    DepartmentResponse response = departmentService.createDepartment(request);
    return ResponseEntity.ok(response);
  }

  // 단건 조회
  @GetMapping("/{id}")
  public ResponseEntity<DepartmentResponse> getDepartment(
      @PathVariable UUID id) {
    DepartmentResponse response = departmentService.getDepartment(id);

    return ResponseEntity.ok(response);
  }

  // 부서 수정
  @PatchMapping("/{id}")
  public ResponseEntity<DepartmentResponse> patchDepartment(
      @PathVariable UUID id,
      @RequestBody DepartmentUpdateRequest request) {
    DepartmentResponse response = departmentService.updateDepartment(id, request);
    return ResponseEntity.ok(response);
  }

  // 부서 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
    departmentService.deleteDepartment(id);
    return ResponseEntity.noContent().build();
  }

  // 부서 목록 조회 (검색, 정렬, 커서 기반 페이지네이션)
  @GetMapping
  public ResponseEntity<DepartmentSliceResponse> getDepartments(
      @ModelAttribute DepartmentSearchRequest request) {
    DepartmentSliceResponse response = departmentService.getDepartments(request);
    return ResponseEntity.ok(response);
  }
}
