package com.deepsleep.api.service;

import com.deepsleep.api.dto.organization.ClazzRequest;
import com.deepsleep.api.dto.organization.DeptRequest;
import com.deepsleep.api.dto.organization.MajorRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.http.QueryParams;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.ClazzVO;
import com.deepsleep.api.vo.DeptVO;
import com.deepsleep.api.vo.MajorVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrganizationApi {

    private final ApiClient apiClient;

    public OrganizationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<List<DeptVO>> listDepartments() {
        return apiClient.get("/org/dept/list", new TypeReference<Result<List<DeptVO>>>() {});
    }

    public CompletableFuture<Void> createDepartment(DeptRequest request) {
        return apiClient.postVoid("/org/dept", request);
    }

    public CompletableFuture<Void> updateDepartment(Long id, DeptRequest request) {
        return apiClient.putVoid("/org/dept/%d".formatted(id), request);
    }

    public CompletableFuture<Void> deleteDepartment(Long id) {
        return apiClient.delete("/org/dept/%d".formatted(id));
    }

    public CompletableFuture<List<MajorVO>> listMajors(Long deptId) {
        QueryParams queryParams = QueryParams.builder()
                .add("deptId", deptId)
                .build();
        return apiClient.get("/org/major/list", queryParams, new TypeReference<Result<List<MajorVO>>>() {});
    }

    public CompletableFuture<Void> createMajor(MajorRequest request) {
        return apiClient.postVoid("/org/major", request);
    }

    public CompletableFuture<Void> updateMajor(Long id, MajorRequest request) {
        return apiClient.putVoid("/org/major/%d".formatted(id), request);
    }

    public CompletableFuture<Void> deleteMajor(Long id) {
        return apiClient.delete("/org/major/%d".formatted(id));
    }

    public CompletableFuture<List<ClazzVO>> listClazzes(Long majorId) {
        QueryParams queryParams = QueryParams.builder()
                .add("majorId", majorId)
                .build();
        return apiClient.get("/org/clazz/list", queryParams, new TypeReference<Result<List<ClazzVO>>>() {});
    }

    public CompletableFuture<Void> createClazz(ClazzRequest request) {
        return apiClient.postVoid("/org/clazz", request);
    }

    public CompletableFuture<Void> updateClazz(Long id, ClazzRequest request) {
        return apiClient.putVoid("/org/clazz/%d".formatted(id), request);
    }

    public CompletableFuture<Void> deleteClazz(Long id) {
        return apiClient.delete("/org/clazz/%d".formatted(id));
    }
}
