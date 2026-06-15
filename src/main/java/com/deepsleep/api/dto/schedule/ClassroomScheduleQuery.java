package com.deepsleep.api.dto.schedule;

import com.deepsleep.api.http.QueryParams;

public record ClassroomScheduleQuery(
        String semester,
        Integer startWeek,
        Integer endWeek,
        Integer weekday
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("semester", semester)
                .add("startWeek", startWeek)
                .add("endWeek", endWeek)
                .add("weekday", weekday)
                .build();
    }
}
