package com.codeit.hrbank.controller;

import com.codeit.hrbank.dto.employee.*;
import com.codeit.hrbank.entity.employee.EmployeeStatus;
import com.codeit.hrbank.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeDto createEmployee(@RequestPart("employee") @Valid EmployeeCreateRequest request,
                                      @RequestPart(value = "profile", required = false) MultipartFile profile,
                                      HttpServletRequest httpRequest) {
        return employeeService.create(request, profile, extractIpAddress(httpRequest));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeDto updateEmployee(@PathVariable UUID id,
                                      @RequestPart("employee") @Valid EmployeeUpdateRequest request,
                                      @RequestPart(value = "profile", required = false) MultipartFile profile,
                                      HttpServletRequest httpRequest) {
        return employeeService.update(id, request, profile, extractIpAddress(httpRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable UUID id,
                               HttpServletRequest httpRequest) {
        employeeService.delete(id, extractIpAddress(httpRequest));
    }

    @GetMapping("/{id}")
    public EmployeeDto getEmployeeById(@PathVariable UUID id) {
        return employeeService.findById(id);
    }

    @GetMapping("/count")
    public long getEmployeeCount(@RequestParam(required = false) EmployeeStatus status,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return employeeService.countEmployees(status, fromDate, toDate);
    }

    @GetMapping("/stats/distribution")
    public List<EmployeeDistributionDto> getDistribution(@RequestParam(defaultValue = "department") String groupBy,
                                                         @RequestParam(defaultValue = "ACTIVE") EmployeeStatus status) {
        return employeeService.getDistribution(groupBy, status);
    }

    @GetMapping("/status/trend")
    public List<EmployeeTrendDto> getTrend(@RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                           @RequestParam(defaultValue = "month") String unit) {
        return employeeService.getTrend(from, to, unit);
    }


    private String extractIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
