package com.deepsleep.api.vo;

import java.time.LocalDateTime;

public record ExamVO(
        Long id,
        Long courseId,
        String courseName,
        Integer type,
        String typeName,
        LocalDateTime examTime,
        Integer duration,
        Long classroomId,
        String classroomName,
        Long invigilatorId,
        String invigilatorName,
        String invigilatorAvatar,
        String remark
) {
}
