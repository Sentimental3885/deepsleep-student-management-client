package com.deepsleep.api.dto.user;

import java.time.LocalDate;

public record UpdateStudentProfileRequest(
        Long clazzId,
        String position,
        LocalDate entryDate
) {
}
