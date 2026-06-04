package com.deepsleep.api.vo;

import java.math.BigDecimal;

public record TeacherCourseVO(
        Long id,
        String name,
        String code,
        String semester,
        Integer capacity,
        Integer size,
        BigDecimal credit,
        Integer status
) {
}
