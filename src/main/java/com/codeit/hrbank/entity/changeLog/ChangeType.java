package com.codeit.hrbank.entity.changeLog;

import lombok.Getter;

@Getter
public enum ChangeType {
    ALL("전체"),
    CREATED("직원 추가"),
    UPDATED("정보 수정"),
    DELETED("직원 삭제");

    private final String label;

    ChangeType(String label) {
        this.label = label;
    }
}
