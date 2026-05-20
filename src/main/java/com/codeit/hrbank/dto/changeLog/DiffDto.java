package com.codeit.hrbank.dto.changeLog;

public record DiffDto(
        String propertyName,
        String before,
        String after
) {

}
