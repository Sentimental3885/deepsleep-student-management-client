package com.deepsleep.api.vo;

import java.time.LocalDate;

public record TeacherProfileVO(
        String avatar,
        Long deptId,
        String deptName,
        String title,
        LocalDate entryDate
) {
}
