package com.deepsleep.api.dto.selection;

import com.deepsleep.api.http.QueryParams;

import java.math.BigDecimal;

public record SelectionQuery(
        Integer pageNum,
        Integer pageSize,
        String keyword,
        String semester,
        Integer weekday,
        BigDecimal creditMin,
        BigDecimal creditMax,
        Boolean noConflictOnly
) {

    public static SelectionQuery firstPage() {
        return new SelectionQuery(1, 20, null, null, null, null, null, null);
    }

    public QueryParams toQueryParams() {
        return QueryParams.builder()
                .add("pageNum", pageNum)
                .add("pageSize", pageSize)
                .add("keyword", keyword)
                .add("semester", semester)
                .add("weekday", weekday)
                .add("creditMin", creditMin)
                .add("creditMax", creditMax)
                .add("noConflictOnly", noConflictOnly)
                .build();
    }
}
