package com.codeit.hrbank.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentUpdateRequest {

  @NotBlank(message = "부서 이름은 필수입니다.")
  @Size(max = 50)
  private String name;

  private String description;

  @NotNull(message = "설립일은 필수입니다.")
  private LocalDate establishedDate;
}
