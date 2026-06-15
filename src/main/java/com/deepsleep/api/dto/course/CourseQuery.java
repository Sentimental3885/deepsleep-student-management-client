package com.deepsleep.api.dto.course;

import com.deepsleep.api.http.QueryParams;

public record CourseQuery(
        String keyword,
        String semester,
        Integer status,
        Long teacherId,
        Long clazzId,
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("keyword", keyword)
                .add("semester", semester)
                .add("status", status)
                .add("teacherId", teacherId)
                .add("clazzId", clazzId)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
