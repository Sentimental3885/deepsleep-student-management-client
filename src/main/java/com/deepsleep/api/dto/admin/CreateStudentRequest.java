package com.deepsleep.api.dto.admin;

import java.time.LocalDate;

public record CreateStudentRequest(
        Long ssid,
        String name,
        Integer gender,
        Long did,
        Long mid,
        Long zid,
        LocalDate entryDate
) {
}
