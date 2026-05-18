package com.codeit.hrbank.entity.changeLog;

import com.codeit.hrbank.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "employee_change_log_diffs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DetailChangeLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "log_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_employee_change_log_diffs_log"))
    private ChangeLog log;

    @Column(name = "property_name", nullable = false, length = 100)
    private String propertyName;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    private DetailChangeLog(ChangeLog log, String propertyName, String beforeValue, String afterValue) {
        this.log = log;
        this.propertyName = propertyName;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    public static DetailChangeLog create(ChangeLog log, String propertyName, String beforeValue, String afterValue) {
        return new DetailChangeLog(log, propertyName, beforeValue, afterValue);
    }

}
