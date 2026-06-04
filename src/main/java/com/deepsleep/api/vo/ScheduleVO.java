package com.deepsleep.api.vo;

public record ScheduleVO(
        Long courseId,
        String courseName,
        String teacherName,
        String teacherAvatar,
        Integer weekday,
        Integer section,
        Integer startWeek,
        Integer endWeek,
        Long classroomId,
        String classroomName
) {
}
