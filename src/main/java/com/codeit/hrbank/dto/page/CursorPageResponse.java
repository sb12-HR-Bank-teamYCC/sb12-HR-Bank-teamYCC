package com.codeit.hrbank.dto.page;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
  List<T> content;
  String nextCursor;
  String nextIdAfter;
  int size;
  long totalElements;
  boolean hasNext;
}
