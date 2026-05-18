package com.codeit.hrbank.dto.page;

import java.util.List;

public class CursorPageResponse<T> {
  List<T> content;
  String nextCursor;
  long nextIdAfter;
  int size;
  long totalElements;
  boolean hasNext;
}
