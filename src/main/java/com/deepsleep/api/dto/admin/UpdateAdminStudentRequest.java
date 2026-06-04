package com.deepsleep.api.dto.admin;

import java.time.LocalDate;

public record UpdateAdminStudentRequest(
        Long deptId,
        Long majorId,
        Long clazzId,
        String position,
        LocalDate entryDate
) {
}
