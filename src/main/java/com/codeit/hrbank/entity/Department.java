package com.codeit.hrbank.entity;

import com.codeit.hrbank.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Department extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "established_date", nullable = false)
  private LocalDate establishedDate;

  public void update(String name, String description, LocalDate establishedDate) {
    this.name = name;
    this.description = description;
    this.establishedDate = establishedDate;
  }

}
