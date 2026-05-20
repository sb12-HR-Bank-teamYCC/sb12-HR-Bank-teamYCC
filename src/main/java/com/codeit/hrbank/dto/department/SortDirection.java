package com.codeit.hrbank.dto.department;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortDirection {
  ASC("asc"),
  DESC("desc");

  private final String value;

  SortDirection(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public static SortDirection from(String value) {
    for (SortDirection dir : SortDirection.values()) {
      if (dir.value.equalsIgnoreCase(value)) {
        return dir;
      }
    }
    throw new IllegalArgumentException("Invalid sortDirection: " + value);
  }
}