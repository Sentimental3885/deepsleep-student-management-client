package com.deepsleep.api.dto.course;

import java.math.BigDecimal;
import java.util.List;

public record CreateCourseRequest(
        String name,
        Long tid,
        Integer capacity,
        String code,
        String semester,
        BigDecimal credit,
        Integer status,
        String introduction,
        List<Long> zids
) {
}
