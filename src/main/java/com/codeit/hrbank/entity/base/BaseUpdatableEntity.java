package com.codeit.hrbank.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public class BaseUpdatableEntity extends BaseEntity {

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp with time zone default now()")
  protected Instant updatedAt;

  @CreatedDate
  @Column(columnDefinition = "timestamp with time zone default now()", updatable = false, nullable = false)
  protected Instant createdAt;
}
