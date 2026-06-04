package com.deepsleep.api.service;

import com.deepsleep.api.dto.user.UpdateStudentProfileRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.ScheduleVO;
import com.deepsleep.api.vo.StudentProfileVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StudentApi {

    private final ApiClient apiClient;

    public StudentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<StudentProfileVO> getProfile() {
        return apiClient.get("/student/profile", new TypeReference<Result<StudentProfileVO>>() {});
    }

    public CompletableFuture<Void> updateProfile(UpdateStudentProfileRequest request) {
        return apiClient.putVoid("/student/profile", request);
    }

    public CompletableFuture<List<ScheduleVO>> getSchedule() {
        return apiClient.get("/student/schedule", new TypeReference<Result<List<ScheduleVO>>>() {});
    }

    public CompletableFuture<List<ExamVO>> getExams() {
        return apiClient.get("/student/exams", new TypeReference<Result<List<ExamVO>>>() {});
    }
}
