package com.codeit.hrbank.entity.file;

import com.codeit.hrbank.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@Getter @Setter @SuperBuilder @ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class FileMetadata extends BaseEntity {

  @Column(nullable = false, length = 255)
  private String originalName;

  @Column(nullable = false, unique = true, length = 255)
  private String storedName;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false, length = 30)
  private String fileType;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  protected Instant createdAt;
}