package com.deepsleep.api.dto.teacher;

import com.deepsleep.api.http.QueryParams;

public record TeacherOptionQuery(
        String keyword,
        Long deptId,
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("keyword", keyword)
                .add("deptId", deptId)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
