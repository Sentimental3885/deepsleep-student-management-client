package com.deepsleep.api.dto.course;

import java.util.List;

public record CourseClazzUpdateRequest(
        List<Long> clazzIds
) {
}
