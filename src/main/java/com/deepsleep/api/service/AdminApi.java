package com.deepsleep.api.service;

import com.deepsleep.api.dto.PageQuery;
import com.deepsleep.api.dto.admin.CreateStudentRequest;
import com.deepsleep.api.dto.admin.CreateTeacherRequest;
import com.deepsleep.api.dto.admin.UpdateAdminStudentRequest;
import com.deepsleep.api.dto.admin.UpdateAdminTeacherRequest;
import com.deepsleep.api.dto.admin.UpdateAdminUserRequest;
import com.deepsleep.api.dto.admin.UserListQuery;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.PageResult;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.AdminUserDetailVO;
import com.deepsleep.api.vo.AdminUserVO;
import com.deepsleep.api.vo.ExamVO;
import com.deepsleep.api.vo.OperationLogVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

public class AdminApi {

    private final ApiClient apiClient;

    public AdminApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> createStudent(CreateStudentRequest request) {
        return apiClient.postVoid("/admin/student", request);
    }

    public CompletableFuture<Void> createTeacher(CreateTeacherRequest request) {
        return apiClient.postVoid("/admin/teacher", request);
    }

    public CompletableFuture<Void> resetUserPassword(Long userId) {
        return apiClient.putVoid("/admin/user/%d/password/reset".formatted(userId), null);
    }

    public CompletableFuture<Void> deleteUser(Long userId) {
        return apiClient.delete("/admin/user/%d".formatted(userId));
    }

    public CompletableFuture<PageResult<AdminUserVO>> listUsers(UserListQuery query) {
        return apiClient.get(
                "/admin/user/list",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<AdminUserVO>>>() {}
        );
    }

    public CompletableFuture<AdminUserDetailVO> getUser(Long userId) {
        return apiClient.get("/admin/user/%d".formatted(userId), new TypeReference<Result<AdminUserDetailVO>>() {});
    }

    public CompletableFuture<Void> updateUser(Long userId, UpdateAdminUserRequest request) {
        return apiClient.putVoid("/admin/user/%d".formatted(userId), request);
    }

    public CompletableFuture<Void> updateStudent(Long userId, UpdateAdminStudentRequest request) {
        return apiClient.putVoid("/admin/student/%d".formatted(userId), request);
    }

    public CompletableFuture<Void> updateTeacher(Long userId, UpdateAdminTeacherRequest request) {
        return apiClient.putVoid("/admin/teacher/%d".formatted(userId), request);
    }

    public CompletableFuture<PageResult<OperationLogVO>> listLogs(PageQuery query) {
        return apiClient.get(
                "/admin/log/list",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<OperationLogVO>>>() {}
        );
    }

    public CompletableFuture<PageResult<ExamVO>> listExams(PageQuery query) {
        return apiClient.get(
                "/admin/exam/list",
                query.toQueryParams(),
                new TypeReference<Result<PageResult<ExamVO>>>() {}
        );
    }
}
