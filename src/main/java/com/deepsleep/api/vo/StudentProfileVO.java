package com.deepsleep.api.vo;

import java.time.LocalDate;

public record StudentProfileVO(
        String avatar,
        Long deptId,
        String deptName,
        Long majorId,
        String majorName,
        Long clazzId,
        String clazzName,
        String position,
        LocalDate entryDate
) {
}
