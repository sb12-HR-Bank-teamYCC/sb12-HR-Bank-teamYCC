package com.codeit.hrbank.service;

import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.employee.*;
import com.codeit.hrbank.dto.error.HrBankException;
import com.codeit.hrbank.dto.page.CursorPageResponse;
import com.codeit.hrbank.entity.Department;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.entity.employee.EmployeeStatus;
import com.codeit.hrbank.entity.file.FileTypeConst;
import com.codeit.hrbank.mapper.EmployeeMapper;
import com.codeit.hrbank.repository.DepartmentRepository;
import com.codeit.hrbank.repository.EmployeeRepository;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ChangeLogService changeLogService;
    private final EmployeeMapper employeeMapper;
    private final FileMetadataService fileMetadataService;

    @Transactional
    public EmployeeDto create(EmployeeCreateRequest request,
                              MultipartFile profile,
                              String ipAddress) {
        validateEmailNotDuplicated(request.email());
        Department department = findDepartment(request.departmentId());
        FileMetadata profileImage = saveProfileImageIfPresent(profile);
        String employeeNumber = generateEmployeeNumber(request.hireDate());

        Employee employee = employeeMapper.toEntity(
                request,
                employeeNumber,
                department,
                profileImage
        );
        Employee savedEmployee = employeeRepository.save(employee);
        changeLogService.logCreated(
                savedEmployee,
                request.memo(),
                ipAddress
        );

        return employeeMapper.toDto(savedEmployee);

    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(UUID id) {
        Employee employee = findEmployee(id);
        return employeeMapper.toDto(employee);
    }

    @Transactional
    public EmployeeDto update(UUID id, EmployeeUpdateRequest request,
                              MultipartFile profile, String ipAddress) {
        Employee employee = findEmployee(id);
        EmployeeSnapshot before = EmployeeSnapshot.from(employee);

        if (request.email() != null) {
            validateEmailNotDuplicatedExceptSelf(request.email(), id);
        }

        Department department = request.departmentId() == null ? employee.getDepartment()
                : findDepartment(request.departmentId());

        FileMetadata oldProfileImage = employee.getProfileImage();
        FileMetadata newProfileImage = oldProfileImage;

        if (profile != null && !profile.isEmpty()) {
            newProfileImage = saveProfileImageIfPresent(profile);
        }

        String name = request.name() == null ? employee.getName() : request.name();
        String email = request.email() == null ? employee.getEmail() : request.email();
        String position = request.position() == null ? employee.getPosition() : request.position();
        LocalDate hireDate = request.hireDate() == null ? employee.getHireDate() : request.hireDate();
        EmployeeStatus status = request.status() == null ? employee.getStatus() : request.status();

        employee.update(name, email, department, position, hireDate, status, newProfileImage);

        if (profile != null && !profile.isEmpty() && oldProfileImage != null) {
            employeeRepository.flush();
            fileMetadataService.delete(List.of(oldProfileImage));
        }
        changeLogService.logUpdated(before, employee, request.memo(), ipAddress);

        return employeeMapper.toDto(employee);
    }

    @Transactional
    public void delete(UUID id, String ipAddress) {
        Employee employee = findEmployee(id);
        EmployeeSnapshot before = EmployeeSnapshot.from(employee);
        FileMetadata profileImage = employee.getProfileImage();

        employee.removeProfileImage();
        employeeRepository.delete(employee);

        if (profileImage != null) {
            employeeRepository.flush();
            fileMetadataService.delete(List.of(profileImage));
        }

        changeLogService.logDeleted(before, null, ipAddress);
    }

    // 대시보드 관련
    @Transactional(readOnly = true)
    public long countEmployees(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
        LocalDate effectiveToDate = toDate;

        if (fromDate != null && effectiveToDate == null) {
            effectiveToDate = LocalDate.now();
        }
        validateDateRange(fromDate, effectiveToDate);

        return employeeRepository.countEmployees(status, fromDate, effectiveToDate);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDistributionDto> getDistribution(String groupBy, EmployeeStatus status) {
        String normalizedGroupBy = normalizeGroupBy(groupBy);
        EmployeeStatus targetStatus = status == null ? EmployeeStatus.ACTIVE : status;

        List<Object[]> rows = normalizedGroupBy.equals("department")
                ? employeeRepository.countByDepartment(targetStatus)
                : employeeRepository.countByPosition(targetStatus);

        long total = rows.stream().mapToLong(row -> (Long) row[1]).sum();
        if (total == 0L) {
            return List.of();
        }

        return rows.stream()
                .map(row -> {
                    String groupKey = (String) row[0];
                    long count = (Long) row[1];
                    double percentage = Math.round((count * 10000.0 / total)) / 100.0;
                    return new EmployeeDistributionDto(groupKey, count, percentage);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeTrendDto> getTrend(LocalDate from, LocalDate to, String unit) {
        String normalizedUnit = normalizeUnit(unit);

        LocalDate endDate = to == null ? LocalDate.now() : to;

        validateDateRange(from, endDate);
        LocalDate endBucket = getBucketStart(endDate, normalizedUnit);
        LocalDate startBucket = from == null ? calculateDefaultFrom(endBucket, normalizedUnit)
                : getBucketStart(from, normalizedUnit);

        List<LocalDate> hireDates = employeeRepository.findHireDatesUntil(endDate);
        List<LocalDate> buckets = createBuckets(startBucket, endBucket, normalizedUnit);

        long previousCount = 0L;
        List<EmployeeTrendDto> result = new ArrayList<>();

        for (LocalDate bucket : buckets) {
            LocalDate bucketEnd = getBucketEnd(bucket, normalizedUnit);
            if (bucketEnd.isAfter(endDate)) {
                bucketEnd = endDate;
            }
            long count = 0L;
            for (LocalDate hireDate : hireDates) {
                if (!hireDate.isAfter(bucketEnd)) {
                    count++;
                }
            }
            long change = count - previousCount;
            double changeRate = calculateChangeRate(previousCount, count);
            result.add(new EmployeeTrendDto(bucket, count, change, changeRate));
            previousCount = count;
        }
        return result;
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new HrBankException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private Department findDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new HrBankException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private void validateEmailNotDuplicated(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new HrBankException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateEmailNotDuplicatedExceptSelf(String email, UUID id) {
        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new HrBankException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private FileMetadata saveProfileImageIfPresent(MultipartFile profile) {
        if (profile == null || profile.isEmpty()) {
            return null;
        }
        FileMetadata profileImage = fileMetadataService.save(List.of(profile)).get(0);
        profileImage.setFileType(FileTypeConst.PROFILE_IMAGE);
        return profileImage;
    }

    private String generateEmployeeNumber(LocalDate hireDate) {
        String prefix = "EMP-%d-".formatted(hireDate.getYear());

        List<Employee> latestEmployees = employeeRepository.findLatestByEmployeeNumberPrefixForUpdate(
                prefix,
                PageRequest.of(0, 1)
        );

        int nextSequence = 1;

        if (!latestEmployees.isEmpty()) {
            String latestEmployeeNumber = latestEmployees.get(0).getEmployeeNumber();
            int latestSequence = Integer.parseInt(latestEmployeeNumber.substring(prefix.length()));
            nextSequence = latestSequence + 1;
        }

        return "%s%04d".formatted(prefix, nextSequence);
    }

    private String normalizeGroupBy(String groupBy) {
        String value = groupBy == null ? "department" : groupBy.toLowerCase();

        if (!value.equals("department") && !value.equals("position")) {
            throw new HrBankException(ErrorCode.INVALID_GROUP_BY);
        }

        return value;
    }

    private String normalizeUnit(String unit) {
        String value = unit == null ? "month" : unit.toLowerCase();

        if (!List.of("day", "week", "month", "quarter", "year").contains(value)) {
            throw new HrBankException(ErrorCode.INVALID_TIME_UNIT);
        }

        return value;
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new HrBankException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private LocalDate calculateDefaultFrom(LocalDate to, String unit) {
        return switch (unit) {
            case "day" -> to.minusDays(11);
            case "week" -> to.minusWeeks(11);
            case "month" -> to.minusMonths(11);
            case "quarter" -> to.minusMonths(33);
            case "year" -> to.minusYears(11);
            default -> throw new HrBankException(ErrorCode.INVALID_TIME_UNIT);
        };
    }

    private List<LocalDate> createBuckets(LocalDate from, LocalDate to, String unit) {
        List<LocalDate> buckets = new ArrayList<>();

        LocalDate current = from;
        while (!current.isAfter(to)) {
            buckets.add(current);
            current = plusUnit(current, unit);
        }

        return buckets;
    }

    private LocalDate plusUnit(LocalDate date, String unit) {
        return switch (unit) {
            case "day" -> date.plusDays(1);
            case "week" -> date.plusWeeks(1);
            case "month" -> date.plusMonths(1);
            case "quarter" -> date.plusMonths(3);
            case "year" -> date.plusYears(1);
            default -> throw new HrBankException(ErrorCode.INVALID_TIME_UNIT);
        };
    }

    private LocalDate getBucketStart(LocalDate date, String unit) {
        return switch (unit) {
            case "day" -> date;
            case "week" -> date.minusDays(date.getDayOfWeek().getValue() - 1L);
            case "month" -> date.withDayOfMonth(1);
            case "quarter" -> {
                int firstMonthOfQuarter = ((date.getMonthValue() - 1) / 3) * 3 + 1;
                yield LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
            }
            case "year" -> LocalDate.of(date.getYear(), 1, 1);
            default -> throw new HrBankException(ErrorCode.INVALID_TIME_UNIT);
        };
    }

    private LocalDate getBucketEnd(LocalDate bucketStart, String unit) {
        return switch (unit) {
            case "day" -> bucketStart;
            case "week" -> bucketStart.plusWeeks(1).minusDays(1);
            case "month" -> bucketStart.plusMonths(1).minusDays(1);
            case "quarter" -> bucketStart.plusMonths(3).minusDays(1);
            case "year" -> bucketStart.plusYears(1).minusDays(1);
            default -> throw new HrBankException(ErrorCode.INVALID_TIME_UNIT);
        };
    }

    private double calculateChangeRate(long previousCount, long currentCount) {
        if (previousCount == 0L) {
            return currentCount == 0L ? 0.0 : 100.0;
        }

        return Math.round(((currentCount - previousCount) * 10000.0 / previousCount)) / 100.0;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<EmployeeDto> getEmployees(
        String nameOrEmail, String employeeNumber, String departmentName,
        String position, LocalDate hireDateFrom, LocalDate hireDateTo,
        EmployeeStatus status, UUID idAfter, String cursor,
        int size, String sortField, String sortDirection
    ) {
        String normalizedSortField = normalizeSortField(sortField);
        String normalizedSortDir   = normalizeSortDirection(sortDirection);

        // size+1 개 조회해서 다음 페이지 존재 여부 판단
        List<Employee> employees = employeeRepository.findAllWithFilters(
            nameOrEmail, employeeNumber, departmentName, position,
            hireDateFrom, hireDateTo, status,
            cursor, idAfter,
            size + 1, normalizedSortField, normalizedSortDir
        );

        boolean hasNext = employees.size() > size;
        if (hasNext) {
            employees = employees.subList(0, size);
        }

        long totalElements = employeeRepository.countWithFilters(
            nameOrEmail, employeeNumber, departmentName, position,
            hireDateFrom, hireDateTo, status
        );

        String nextCursor   = null;
        String nextIdAfter  = null;

        if (!employees.isEmpty()) {
            Employee last = employees.get(employees.size() - 1);
            nextCursor  = encodeCursor(last, normalizedSortField);
            nextIdAfter = last.getId().toString();
        }

        List<EmployeeDto> content = employees.stream()
            .map(employeeMapper::toDto)
            .toList();

        return new CursorPageResponse<>(content, nextCursor, nextIdAfter, size, totalElements, hasNext);
    }

// ── 유틸 ──────────────────────────────────────────────────────────────────────

    private String encodeCursor(Employee employee, String sortField) {
        String raw = switch (sortField) {
            case "employeeNumber" -> employee.getEmployeeNumber();
            case "hireDate"       -> employee.getHireDate().toString();
            default               -> employee.getName();
        };
        return Base64.getEncoder().encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String normalizeSortField(String sortField) {
        if (sortField == null) return "name";
        return switch (sortField.toLowerCase()) {
            case "employeenumber" -> "employeeNumber";
            case "hiredate"       -> "hireDate";
            default               -> "name";
        };
    }

    private String normalizeSortDirection(String sortDirection) {
        return "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";
    }
}
