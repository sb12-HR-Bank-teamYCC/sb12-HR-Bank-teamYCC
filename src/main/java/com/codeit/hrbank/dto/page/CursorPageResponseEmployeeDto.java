package com.codeit.hrbank.dto.page;

import com.codeit.hrbank.dto.employee.EmployeeDto;

import java.util.List;
import java.util.UUID;

public record CursorPageResponseEmployeeDto(
        List<EmployeeDto> content,
        String nextCursor,
        UUID nextIdAfter,
        int size,
        long totalElements,
        boolean hasNext
) {

}
