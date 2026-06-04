package com.deepsleep.api.dto.course;

public record UpdateCourseRequest(
        Integer capacity,
        Integer status,
        String introduction
) {
}
