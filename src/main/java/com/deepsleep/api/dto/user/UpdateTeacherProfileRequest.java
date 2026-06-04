package com.deepsleep.api.dto.user;

import java.time.LocalDate;

public record UpdateTeacherProfileRequest(
        String title,
        LocalDate entryDate
) {
}
