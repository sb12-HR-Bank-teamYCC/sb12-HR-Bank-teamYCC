package com.codeit.hrbank.dto.department;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
  private UUID id;
  private String name;
  private String description;
  private LocalDate establishedDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int employeeCount;
}
