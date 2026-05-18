package com.codeit.hrbank.entity.changeLog;

import com.codeit.hrbank.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "employee_change_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChangeType type;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_number", nullable = false, length = 30)
    private String employeeNumber;

    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "at", nullable = false)
    private Instant at;

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailChangeLog> diffs = new ArrayList<>();

    private ChangeLog(ChangeType type, UUID employeeId, String employeeNumber, String employeeName, String memo,
                      String ipAddress, Instant at) {
        this.type = type;
        this.employeeId = employeeId;
        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.memo = memo;
        this.ipAddress = ipAddress;
        this.at = at;
    }

    public static ChangeLog create(ChangeType type, UUID employeeId, String employeeNumber, String employeeName,
                                   String memo, String ipAddress) {
        return new ChangeLog(
                type,
                employeeId,
                employeeNumber,
                employeeName,
                memo,
                ipAddress,
                Instant.now()
        );
    }

    public void addDiff(String propertyName, String beforeValue, String afterValue) {
        DetailChangeLog diff = DetailChangeLog.create(
                this,
                propertyName,
                beforeValue,
                afterValue
        );
        this.diffs.add(diff);
    }

}
