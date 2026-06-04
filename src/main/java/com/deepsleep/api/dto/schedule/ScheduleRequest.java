package com.deepsleep.api.dto.schedule;

public record ScheduleRequest(
        Integer weekday,
        Integer section,
        Integer startWeek,
        Integer endWeek,
        Long rid
) {
}
