package com.deepsleep.api.dto.admin;

import java.time.LocalDate;

public record UpdateAdminTeacherRequest(
        Long deptId,
        String title,
        LocalDate entryDate
) {
}
