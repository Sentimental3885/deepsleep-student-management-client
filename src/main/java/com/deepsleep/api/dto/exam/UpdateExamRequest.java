package com.deepsleep.api.dto.exam;

import java.time.LocalDateTime;

public record UpdateExamRequest(
        Integer type,
        LocalDateTime examTime,
        Integer duration,
        Long classroomId,
        Long invigilatorId,
        String remark
) {
}
