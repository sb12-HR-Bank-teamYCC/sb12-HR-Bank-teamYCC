package com.codeit.hrbank.dto.employee;

import java.time.LocalDate;

public record EmployeeTrendDto(
        LocalDate date,
        long count,
        long change,
        double changeRate
) {

}
