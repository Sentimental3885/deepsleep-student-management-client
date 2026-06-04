package com.deepsleep.api.service;

import com.deepsleep.api.dto.course.CreateCourseRequest;
import com.deepsleep.api.dto.course.UpdateCourseRequest;
import com.deepsleep.api.dto.schedule.ScheduleRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.CourseVO;
import com.deepsleep.api.vo.ScheduleVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CourseApi {

    private final ApiClient apiClient;

    public CourseApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> addCourse(CreateCourseRequest request) {
        return apiClient.postVoid("/course/add", request);
    }

    public CompletableFuture<CourseVO> getCourse(Long courseId) {
        return apiClient.get("/course/detail/%d".formatted(courseId), new TypeReference<Result<CourseVO>>() {});
    }

    public CompletableFuture<Void> deleteCourse(Long courseId) {
        return apiClient.delete("/course/delete/%d".formatted(courseId));
    }

    public CompletableFuture<Void> updateCourse(Long courseId, UpdateCourseRequest request) {
        return apiClient.putVoid("/course/update/%d".formatted(courseId), request);
    }

    public CompletableFuture<List<ScheduleVO>> listSchedules(Long courseId) {
        return apiClient.get(
                "/course/schedule/%d".formatted(courseId),
                new TypeReference<Result<List<ScheduleVO>>>() {}
        );
    }

    public CompletableFuture<Void> createSchedule(Long courseId, ScheduleRequest request) {
        return apiClient.postVoid("/course/schedule/%d".formatted(courseId), request);
    }

    public CompletableFuture<Void> updateSchedule(Long courseId, Long scheduleId, ScheduleRequest request) {
        return apiClient.putVoid("/course/schedule/%d/%d".formatted(courseId, scheduleId), request);
    }

    public CompletableFuture<Void> deleteSchedule(Long courseId, Long scheduleId) {
        return apiClient.delete("/course/schedule/%d/%d".formatted(courseId, scheduleId));
    }
}
