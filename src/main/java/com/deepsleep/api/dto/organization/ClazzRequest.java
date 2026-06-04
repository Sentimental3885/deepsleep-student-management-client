package com.deepsleep.api.dto.organization;

public record ClazzRequest(
        String name,
        Long deptId,
        Long majorId,
        Integer grade
) {
}
