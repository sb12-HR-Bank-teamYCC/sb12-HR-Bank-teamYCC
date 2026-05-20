package com.codeit.hrbank.entity.employee;

import com.codeit.hrbank.entity.Department;
import com.codeit.hrbank.entity.file.FileMetadata;
import com.codeit.hrbank.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employees_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_employees_employee_number", columnNames = "employee_number"),
        @UniqueConstraint(name = "uk_employees_profile_image_id", columnNames = "profile_image_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Employee extends BaseUpdatableEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "employee_number", nullable = false, length = 30, updatable = false)
    private String employeeNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_employees_department"))
    private Department department;

    @Column(name = "position", nullable = false, length = 50)
    private String position;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id", unique = true,
                foreignKey = @ForeignKey(name = "fk_employees_profile_image"))
    private FileMetadata profileImage;

    public void update(String name, String email, Department department,
                                  String position, LocalDate hireDate, EmployeeStatus status, FileMetadata profileImage) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.position = position;
        this.hireDate = hireDate;
        this.status = status;
        this.profileImage = profileImage;
    }

    public void changeProfileImage(FileMetadata profileImage) {
        this.profileImage = profileImage;
    }

    public void removeProfileImage() {
        this.profileImage = null;
    }

    public void resign() {
        this.status = EmployeeStatus.RESIGNED;
    }
}
