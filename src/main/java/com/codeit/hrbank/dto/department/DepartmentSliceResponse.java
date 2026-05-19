package com.codeit.hrbank.dto.department;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSliceResponse {
  private List<DepartmentResponse> content;
  private String nextCursor;
  private UUID nextIdAfter;
  private int size;
  private long totalElements;
  private boolean hasNext;
  private String sortField;
  private String sortDirection;
}
