package com.deepsleep.api.service;

import com.deepsleep.api.dto.schedule.ClassroomScheduleQuery;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.vo.ScheduleVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScheduleApi {

    private final ApiClient apiClient;

    public ScheduleApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<List<ScheduleVO>> listClassroomSchedule(Long classroomId, ClassroomScheduleQuery query) {
        return apiClient.get(
                "/schedule/classroom/%d".formatted(classroomId),
                query.toQueryParams(),
                new TypeReference<>() {}
        );
    }
}
