package com.codeit.hrbank.controller;

import com.codeit.hrbank.dto.changeLog.ChangeLogDetailDto;
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

    @GetMapping("/{id}")
    public ChangeLogDetailDto getChangeLogDetail(@PathVariable UUID id) {
        return changeLogService.findDetail(id);
    }

    @GetMapping("/count")
    public long getChangeLogsCount(@RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)Instant fromDate,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate) {
        return changeLogService.countChangeLogs(fromDate, toDate);
    }

}
