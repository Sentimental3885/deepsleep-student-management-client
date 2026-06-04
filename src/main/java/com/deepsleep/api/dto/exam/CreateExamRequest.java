package com.deepsleep.api.dto.exam;

import java.time.LocalDateTime;

public record CreateExamRequest(
        Long courseId,
        Integer type,
        LocalDateTime examTime,
        Integer duration,
        Long classroomId,
        Long invigilatorId,
        String remark
) {
}
