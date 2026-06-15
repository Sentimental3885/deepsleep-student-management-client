package com.deepsleep.api.vo;

import java.util.List;

public record SelectionCheckVO(
        Boolean selectable,
        String reasonCode,
        String reason,
        List<ScheduleVO> conflictSchedules
) {
}
