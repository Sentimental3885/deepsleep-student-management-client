package com.deepsleep.api.dto.selection;

import com.deepsleep.api.http.QueryParams;

public record CourseStudentQuery(
        String keyword,
        Integer status,
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("keyword", keyword)
                .add("status", status)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
