package com.deepsleep.api.service;

import com.deepsleep.api.dto.user.UpdateTeacherProfileRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.TeacherCourseVO;
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
        return apiClient.get("/teacher/profile", new TypeReference<Result<TeacherProfileVO>>() {});
    }

    public CompletableFuture<Void> updateProfile(UpdateTeacherProfileRequest request) {
        return apiClient.putVoid("/teacher/profile", request);
    }

    public CompletableFuture<List<TeacherCourseVO>> getCourses() {
        return apiClient.get("/teacher/courses", new TypeReference<Result<List<TeacherCourseVO>>>() {});
    }

    public CompletableFuture<List<ExamVO>> getExams() {
        return apiClient.get("/teacher/exams", new TypeReference<Result<List<ExamVO>>>() {});
    }
}
