package com.deepsleep.api.service;

import com.deepsleep.api.dto.user.SendEmailCodeRequest;
import com.deepsleep.api.dto.user.SendPhoneCodeRequest;
import com.deepsleep.api.dto.user.UpdateEmailRequest;
import com.deepsleep.api.dto.user.UpdatePasswordRequest;
import com.deepsleep.api.dto.user.UpdatePhoneRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.vo.AvatarVO;
import com.deepsleep.api.vo.MyUserInfoVO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class UserApi {

    private final ApiClient apiClient;

    public UserApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<MyUserInfoVO> getCurrentUser() {
        return apiClient.get("/user/me", new TypeReference<>() {});
    }

    public CompletableFuture<Void> sendEmailCode(SendEmailCodeRequest request) {
        return apiClient.postVoid("/user/email/code", request);
    }

    public CompletableFuture<Void> updateEmail(UpdateEmailRequest request) {
        return apiClient.putVoid("/user/email", request);
    }

    public CompletableFuture<Void> sendPhoneCode(SendPhoneCodeRequest request) {
        return apiClient.postVoid("/user/phone/code", request);
    }

    public CompletableFuture<Void> updatePhone(UpdatePhoneRequest request) {
        return apiClient.putVoid("/user/phone", request);
    }

    public CompletableFuture<Void> sendPasswordCode() {
        return apiClient.postVoid("/user/password/code", null);
    }

    public CompletableFuture<Void> updatePassword(UpdatePasswordRequest request) {
        return apiClient.putVoid("/user/password", request);
    }

    public CompletableFuture<AvatarVO> uploadAvatar(Path avatarFile) {
        return apiClient.upload("/user/avatar", "avatar", avatarFile, new TypeReference<>() {});
    }
}
