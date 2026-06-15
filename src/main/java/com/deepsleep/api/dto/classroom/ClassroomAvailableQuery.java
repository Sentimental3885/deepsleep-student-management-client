package com.deepsleep.api.dto.classroom;

import com.deepsleep.api.http.QueryParams;

public record ClassroomAvailableQuery(
        Integer weekday,
        Integer section,
        Integer startWeek,
        Integer endWeek,
        String semester
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("weekday", weekday)
                .add("section", section)
                .add("startWeek", startWeek)
                .add("endWeek", endWeek)
                .add("semester", semester)
                .build();
    }
}
