package com.codeit.hrbank.service;

import com.codeit.hrbank.dto.employee.EmployeeCreateRequest;
import com.codeit.hrbank.dto.employee.EmployeeDto;
import com.codeit.hrbank.dto.employee.EmployeeUpdateRequest;
import com.codeit.hrbank.entity.Department;
import com.codeit.hrbank.entity.FileMetadata;
import com.codeit.hrbank.entity.employee.Employee;
import com.codeit.hrbank.entity.employee.EmployeeStatus;
import com.codeit.hrbank.repository.DepartmentRepository;
import com.codeit.hrbank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ChangeLogService changeLogService;
//    private final FileMetadataService fileMetadataService;

    @Transactional
    public EmployeeDto create(EmployeeCreateRequest request,
                              MultipartFile profile,
                              String ipAddress) {
        validateEmailNotDuplicated(request.email());
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NoSuchElementException("부서를 찾을 수 없습니다."));
        FileMetadata profileImage = null;
        if (profile != null && !profile.isEmpty()) {
//            profileImage = fileMetadataService.storeProfileImage(profile);
        }
        String employeeNumber = generateEmployeeNumber();

        Employee employee = Employee.create(
                request.name(),
                request.email(),
                employeeNumber,
                department,
                request.position(),
                request.hireDate(),
                profileImage
        );
        Employee savedEmployee = employeeRepository.save(employee);
        changeLogService.logCreated(
                savedEmployee,
                request.memo(),
                ipAddress
        );

        return EmployeeDto.from(savedEmployee);

    }

    @Transactional(readOnly = true)
    public EmployeeDto findById(UUID id) {
        Employee employee = findEmployee(id);
        return EmployeeDto.from(employee);
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
//            newProfileImage = profileImageStorage.store(profile);
        }

        String name = request.name() == null ? employee.getName() : request.name();
        String email = request.email() == null ? employee.getEmail() : request.email();
        String position = request.position() == null ? employee.getPosition() : request.position();
        LocalDate hireDate = request.hireDate() == null ? employee.getHireDate() : request.hireDate();
        EmployeeStatus status = request.status() == null ? employee.getStatus() : request.status();

        employee.update(name, email, department, position, hireDate, status, newProfileImage);

        if (profile != null && !profile.isEmpty() && oldProfileImage != null) {
//            profileImageStorage.delete(oldProfileImage);
        }
        changeLogService.logUpdated(before, employee, request.memo(), ipAddress);

        return EmployeeDto.from(employee);
    }

    @Transactional
    public void delete(UUID id, String ipAddress) {
        Employee employee = findEmployee(id);
        EmployeeSnapshot before = EmployeeSnapshot.from(employee);
        FileMetadata profileImage = employee.getProfileImage();

        employee.removeProfileImage();
        employeeRepository.delete(employee);

        if (profileImage != null) {
//            profileImageStorage.delete(profileImage);
        }

        changeLogService.logDeleted(before, null, ipAddress);
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."));
    }

    private Department findDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "부서를 찾을 수 없습니다."));
    }

    private void validateEmailNotDuplicated(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일립니다.");
        }
    }

    private void validateEmailNotDuplicatedExceptSelf(String email, UUID id) {
        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일립니다.");
        }
    }

    private FileMetadata saveProfileImageIfPresent(MultipartFile profile) {
        if (profile == null || profile.isEmpty()) {
            return null;
        }
//        return profileImageStorage.store(profile);
    }

    private String generateEmployeeNumber() {
        return "EMP-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
