package com.codeit.hrbank.controller;

import com.codeit.hrbank.dto.changeLog.ChangeLogDetailDto;
import com.codeit.hrbank.dto.changeLog.ChangeLogDto;
import com.codeit.hrbank.dto.page.CursorPageResponse;
import com.codeit.hrbank.entity.changeLog.ChangeType;
import com.codeit.hrbank.service.ChangeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/change-logs")
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    @GetMapping
    public CursorPageResponse<ChangeLogDto> getAllChangeLogs(
            @RequestParam(required = false) String employeeNumber,
            @RequestParam(required = false) ChangeType type,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant atFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant atTo,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "at") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return changeLogService.getChangeLogs(
                employeeNumber, type, memo, ipAddress,
                atFrom, atTo,
                idAfter, cursor,
                size, sortField, sortDirection
        );
    }

    @GetMapping("/{id}")
    public ChangeLogDetailDto getChangeLogDetail(@PathVariable UUID id) {
        return changeLogService.findDetail(id);
    }

    @GetMapping("/count")
    public long getChangeLogsCount(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
    ) {
        return changeLogService.countChangeLogs(fromDate, toDate);
    }
}
