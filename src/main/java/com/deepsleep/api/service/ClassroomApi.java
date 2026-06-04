package com.deepsleep.api.service;

import com.deepsleep.api.dto.classroom.ClassroomRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.ClassroomVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClassroomApi {

    private final ApiClient apiClient;

    public ClassroomApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<List<ClassroomVO>> listClassrooms() {
        return apiClient.get("/classroom/list", new TypeReference<Result<List<ClassroomVO>>>() {});
    }

    public CompletableFuture<Void> createClassroom(ClassroomRequest request) {
        return apiClient.postVoid("/classroom", request);
    }

    public CompletableFuture<Void> updateClassroom(Long id, ClassroomRequest request) {
        return apiClient.putVoid("/classroom/%d".formatted(id), request);
    }

    public CompletableFuture<Void> deleteClassroom(Long id) {
        return apiClient.delete("/classroom/%d".formatted(id));
    }
}
