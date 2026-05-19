package com.codeit.hrbank.entity.changeLog;

import com.codeit.hrbank.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "employee_change_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
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

    @Builder.Default
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailChangeLog> diffs = new ArrayList<>();

    public void addDiff(String propertyName, String beforeValue, String afterValue) {
        DetailChangeLog diff = DetailChangeLog.builder()
                .log(this)
                .propertyName(propertyName)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .build();

        this.diffs.add(diff);
    }

}
