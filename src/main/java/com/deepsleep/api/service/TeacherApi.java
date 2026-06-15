package com.deepsleep.api.service;

import com.deepsleep.api.dto.teacher.TeacherOptionQuery;
import com.deepsleep.api.dto.user.UpdateTeacherProfileRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.PageResult;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.ScheduleVO;
import com.deepsleep.api.vo.TeacherCourseVO;
import com.deepsleep.api.vo.TeacherOptionVO;
import com.deepsleep.api.vo.TeacherProfileVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TeacherApi {

    private final ApiClient apiClient;

    public TeacherApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<TeacherProfileVO> getProfile() {
        return apiClient.get("/teacher/profile", new TypeReference<>() {});
    }

    public CompletableFuture<Void> updateProfile(UpdateTeacherProfileRequest request) {
        return apiClient.putVoid("/teacher/profile", request);
    }

    public CompletableFuture<List<TeacherCourseVO>> getCourses() {
        return apiClient.get("/teacher/courses", new TypeReference<>() {});
    }

    public CompletableFuture<PageResult<TeacherOptionVO>> listOptions(TeacherOptionQuery query) {
        return apiClient.get(
                "/teacher/options",
                query.toQueryParams(),
                new TypeReference<>() {}
        );
    }

    public CompletableFuture<List<ScheduleVO>> getSchedule() {
        return apiClient.get("/teacher/schedule", new TypeReference<>() {});
    }

    public CompletableFuture<List<ExamVO>> getExams() {
        return apiClient.get("/teacher/exams", new TypeReference<>() {});
    }
}
