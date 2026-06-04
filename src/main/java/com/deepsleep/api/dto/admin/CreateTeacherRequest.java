package com.deepsleep.api.dto.admin;

import java.time.LocalDate;

public record CreateTeacherRequest(
        Long tsid,
        String name,
        Integer gender,
        Long did,
        String title,
        LocalDate entryDate
) {
}
