package com.codeit.hrbank.service;

import com.codeit.hrbank.common.exception.ErrorCode;
import com.codeit.hrbank.dto.changeLog.ChangeLogDetailDto;
import com.codeit.hrbank.dto.error.HrBankException;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.changeLog.ChangeLog;
import com.codeit.hrbank.entity.changeLog.ChangeType;
import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.mapper.ChangeLogMapper;
import com.codeit.hrbank.repository.ChangeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeLogService {

    private final ChangeLogRepository changeLogRepository;
    private final ChangeLogMapper changeLogMapper;

    @Transactional
    public ChangeLog logCreated(Employee employee, String memo, String ipAddress) {

        ChangeLog log = buildLog(ChangeType.CREATED, employee, memo, ipAddress);

        log.addDiff("이름", null, employee.getName());
        log.addDiff("이메일", null, employee.getEmail());
        log.addDiff("부서", null, employee.getDepartment().getName());
        log.addDiff("직함", null, employee.getPosition());
        log.addDiff("입사일", null, employee.getHireDate().toString());
        log.addDiff("상태", null, employee.getStatus().name());

        if (employee.getProfileImage() != null) {
            log.addDiff("프로필 이미지", null, employee.getProfileImage().getId().toString());
        }

        return changeLogRepository.save(log);
    }

    @Transactional
    public void logUpdated(EmployeeSnapshot before, Employee after, String memo, String ipAddress) {

        ChangeLog log = buildLog(ChangeType.UPDATED, after, memo, ipAddress);

        addDiffIfChanged(log, "이름", before.name(), after.getName());
        addDiffIfChanged(log, "이메일", before.email(), after.getEmail());
        addDiffIfChanged(log, "부서", before.departmentName(), after.getDepartment().getName());
        addDiffIfChanged(log, "직함", before.position(), after.getPosition());
        addDiffIfChanged(log, "입사일", before.hireDate(), toString(after.getHireDate()));
        addDiffIfChanged(log, "상태", before.status(), after.getStatus().name());
        addDiffIfChanged(log, "프로필 이미지", before.profileImageId(), getProfileImageId(after));

        if (!log.getDiffs().isEmpty()) {
            changeLogRepository.save(log);
        }
    }

    @Transactional
    public void logDeleted(EmployeeSnapshot before, String memo, String ipAddress) {

        ChangeLog log = buildLog(ChangeType.DELETED, before, memo, ipAddress);

        log.addDiff("이름", before.name(), null);
        log.addDiff("이메일", before.email(), null);
        log.addDiff("부서", before.departmentName(), null);
        log.addDiff("직함", before.position(), null);
        log.addDiff("입사일", before.hireDate(), null);
        log.addDiff("상태", before.status(), null);

        if (before.profileImageId() != null) {
            log.addDiff("프로필 이미지", before.profileImageId(), null);
        }
        changeLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public ChangeLogDetailDto findDetail(UUID id) {
        ChangeLog changeLog = changeLogRepository.findWithDiffsById(id)
                .orElseThrow(() -> new HrBankException(ErrorCode.CHANGE_LOG_NOT_FOUND));
        return changeLogMapper.toDetailDto(changeLog);
    }

    @Transactional(readOnly = true)
    public long countChangeLogs(Instant fromDate, Instant toDate) {
        Instant effectiveToDate = toDate == null ? Instant.now() : toDate;
        Instant effectiveFromDate = fromDate == null
                ? effectiveToDate.minus(7, ChronoUnit.DAYS) : fromDate;

        if (effectiveFromDate.isAfter(effectiveToDate)) {
            throw new HrBankException(ErrorCode.INVALID_DATE_RANGE);
        }

        return changeLogRepository.countByAtBetween(effectiveFromDate, effectiveToDate);
    }

    private ChangeLog buildLog(ChangeType type, Employee employee, String memo, String ipAddress
    ) {
        return ChangeLog.builder()
            .type(type)
            .employeeId(employee.getId())
            .employeeNumber(employee.getEmployeeNumber())
            .employeeName(employee.getName())
            .memo(memo)
            .ipAddress(ipAddress)
            .at(Instant.now())
            .build();
    }

    private ChangeLog buildLog(ChangeType type, EmployeeSnapshot snapshot, String memo, String ipAddress
    ) {

        return ChangeLog.builder()
            .type(type)
            .employeeId(snapshot.id())
            .employeeNumber(snapshot.employeeNumber())
            .employeeName(snapshot.name())
            .memo(memo)
            .ipAddress(ipAddress)
            .at(Instant.now())
            .build();
    }

    private void addDiffIfChanged(ChangeLog log, String propertyName, String beforeValue,
        String afterValue) {
        if (!Objects.equals(beforeValue, afterValue)) {
            log.addDiff(propertyName, beforeValue, afterValue);
        }
    }

    private String getProfileImageId(Employee employee) {
        FileMetadata profileImage = employee.getProfileImage();
        if (profileImage == null) {
            return null;
        }
        return profileImage.getId().toString();
    }

    private String toString(LocalDate value) {
        return value == null ? null : value.toString();
    }
}