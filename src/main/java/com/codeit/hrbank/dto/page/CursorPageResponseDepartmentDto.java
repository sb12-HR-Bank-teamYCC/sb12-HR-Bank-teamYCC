package com.codeit.hrbank.dto.page;

import com.codeit.hrbank.entity.Department;
import java.util.List;
import java.util.UUID;

public record CursorPageResponseDepartmentDto(
    List<Department> content,
    String nextCursor,
    UUID nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
