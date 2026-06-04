package com.deepsleep.api.dto;

import com.deepsleep.api.http.QueryParams;

public record PageQuery(
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
