package com.deepsleep.api.vo;

public record TeacherOptionVO(
        Long id,
        String username,
        String name,
        Long deptId,
        String deptName,
        String title
) {
}
