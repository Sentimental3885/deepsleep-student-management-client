package com.deepsleep.api.service;

import com.deepsleep.api.dto.auth.EmailCodeRequest;
import com.deepsleep.api.http.ApiClient;

import java.util.concurrent.CompletableFuture;

public class EmailApi {

    private final ApiClient apiClient;

    public EmailApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<Void> sendCode(EmailCodeRequest request) {
        return apiClient.postVoid("/email/code", request);
    }
}
