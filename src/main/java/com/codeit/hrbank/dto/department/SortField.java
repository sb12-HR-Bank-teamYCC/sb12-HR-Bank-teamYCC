package com.codeit.hrbank.dto.department;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortField {
  NAME("name"),
  ESTABLISHED_DATE("establishedDate");

  private final String value;

  SortField(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
