package com.deepsleep.api.dto.admin;

import com.deepsleep.api.http.QueryParams;

public record UserListQuery(
        String name,
        String username,
        Integer role,
        Integer pageNum,
        Integer pageSize
) {

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("name", name)
                .add("username", username)
                .add("role", role)
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .build();
    }
}
