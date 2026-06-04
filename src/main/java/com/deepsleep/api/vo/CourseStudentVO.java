package com.deepsleep.api.vo;

import java.math.BigDecimal;

public record CourseStudentVO(
        Long studentId,
        String studentName,
        String studentAvatar,
        String username,
        BigDecimal score,
        String selectionStatus
) {
}
