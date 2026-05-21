package com.codeit.hrbank.dto.employee;

public record EmployeeDistributionDto(
        String groupKey,
        long count,
        double percentage
) {

}
