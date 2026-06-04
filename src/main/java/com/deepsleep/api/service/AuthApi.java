package com.deepsleep.api.service;

import com.deepsleep.api.dto.auth.LoginRequest;
import com.deepsleep.api.http.ApiClient;
import com.deepsleep.api.result.Result;
import com.deepsleep.api.vo.LoginVO;
import com.deepsleep.auth.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

public class AuthApi {

    private final ApiClient apiClient;
    private final TokenStore tokenStore;

    public AuthApi(ApiClient apiClient, TokenStore tokenStore) {
        this.apiClient = apiClient;
        this.tokenStore = tokenStore;
    }

    public CompletableFuture<LoginVO> login(LoginRequest request) {
        return apiClient.post("/auth/login", request, new TypeReference<Result<LoginVO>>() {})
                .thenApply(loginVO -> {
                    tokenStore.setAccessToken(loginVO.token());
                    return loginVO;
                });
    }

    public CompletableFuture<Void> logout() {
        return apiClient.postVoid("/auth/logout", null)
                .whenComplete((ignored, throwable) -> tokenStore.clear());
    }
}
