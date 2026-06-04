package com.deepsleep.api.vo;

public record ClazzVO(
        Long id,
        String name,
        Long deptId,
        String deptName,
        Long majorId,
        String majorName,
        Integer grade
) {
}
