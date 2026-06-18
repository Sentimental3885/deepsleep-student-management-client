package com.deepsleep.api.vo;

import java.math.BigDecimal;
import java.util.List;

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
        String introduction,
        List<ClazzVO> clazzes,
        Boolean selectable,
        String unselectableReason,
        Integer mySelectionStatus
) {
}
