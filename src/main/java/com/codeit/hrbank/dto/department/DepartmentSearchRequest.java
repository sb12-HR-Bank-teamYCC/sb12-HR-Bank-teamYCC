package com.codeit.hrbank.dto.department;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSearchRequest {
  private String nameOrDescription;
  private UUID idAfter;
  private String cursor;

  @Builder.Default
  private int Size = 10;

  @Builder.Default
  private SortField sortField = SortField.ESTABLISHED_DATE;

  @Builder.Default
  private SortDirection sortDirection = SortDirection.ASC;

}
