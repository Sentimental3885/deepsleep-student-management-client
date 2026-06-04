package com.deepsleep.api.dto.auth;

public record LoginRequest(
        String username,
        String password
) {
}
