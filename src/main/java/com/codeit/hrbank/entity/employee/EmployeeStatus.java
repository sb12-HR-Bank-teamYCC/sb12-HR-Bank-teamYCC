package com.codeit.hrbank.entity.employee;

import lombok.Getter;

// 직원 정보 열거형
@Getter
public enum EmployeeStatus {
    ACTIVE("재직중"),
    ON_LEAVE("휴직중"),
    RESIGNED("퇴사");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }

}
