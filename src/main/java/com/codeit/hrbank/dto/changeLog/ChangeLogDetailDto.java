package com.codeit.hrbank.dto.changeLog;

import com.codeit.hrbank.entity.changeLog.ChangeType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChangeLogDetailDto(
        UUID id,
        ChangeType type,
        String employeeNumber,
        String employeeName,
        String memo,
        String ipAddress,
        Instant at,
        List<DiffDto> diffs
) {

}
