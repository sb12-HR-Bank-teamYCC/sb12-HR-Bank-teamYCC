package com.codeit.hrbank.dto.department;


import java.time.Instant;
import java.time.LocalDate;
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
public class DepartmentDto {
  private UUID id;
  private String name;
  private String description;
  private LocalDate establishedDate;
  private Instant createdAt;
  private Instant updatedAt;
  private int employeeCount;
}
