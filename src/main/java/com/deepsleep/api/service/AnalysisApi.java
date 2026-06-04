package com.deepsleep.api.service;

import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.GradeAnalysisVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnalysisApi {

    private final ApiClient apiClient;

    public AnalysisApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<GradeAnalysisVO> analyzeMyGrades() {
        return apiClient.post("/analysis/me", null, new TypeReference<Result<GradeAnalysisVO>>() {});
    }

    public CompletableFuture<List<GradeAnalysisVO>> listMyAnalysisHistory() {
        return apiClient.get("/analysis/me/history", new TypeReference<Result<List<GradeAnalysisVO>>>() {});
    }
}
