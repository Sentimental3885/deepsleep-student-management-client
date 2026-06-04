package com.deepsleep.api.service;

import com.deepsleep.api.dto.exam.CreateExamRequest;
import com.deepsleep.api.dto.exam.UpdateExamRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.ExamVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

public class ExamApi {

    private final ApiClient apiClient;

    public ExamApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> createExam(CreateExamRequest request) {
        return apiClient.postVoid("/exam", request);
    }

    public CompletableFuture<Void> updateExam(Long examId, UpdateExamRequest request) {
        return apiClient.putVoid("/exam/%d".formatted(examId), request);
    }

    public CompletableFuture<Void> deleteExam(Long examId) {
        return apiClient.delete("/exam/%d".formatted(examId));
    }

    public CompletableFuture<ExamVO> getExam(Long examId) {
        return apiClient.get("/exam/%d".formatted(examId), new TypeReference<Result<ExamVO>>() {});
    }
}
