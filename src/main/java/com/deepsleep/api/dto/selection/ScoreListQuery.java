package com.deepsleep.api.dto.selection;

import com.deepsleep.api.http.QueryParams;

public record ScoreListQuery(
        String semester,
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("semester", semester)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
