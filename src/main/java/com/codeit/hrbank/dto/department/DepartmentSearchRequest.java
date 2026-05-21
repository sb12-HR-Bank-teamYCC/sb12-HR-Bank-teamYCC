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
  private int size = 10;

  @Builder.Default
  private String sortField = "establishedDate";

  @Builder.Default
  private String sortDirection = "asc";

  public SortField getSortFieldEnum() {
    return SortField.from(sortField);
  }

  public SortDirection getSortDirectionEnum() {
    return SortDirection.from(sortDirection);
  }
}
