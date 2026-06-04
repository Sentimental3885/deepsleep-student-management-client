package com.deepsleep.api.vo;

import java.math.BigDecimal;

public record CourseVO(
        Long id,
        String name,
        Long teacherId,
        String teacherName,
        String teacherAvatar,
        String code,
        Integer capacity,
        Integer size,
        String semester,
        BigDecimal credit,
        Integer status,
        String introduction
) {
}
