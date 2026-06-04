package com.deepsleep.api.dto.user;

public record UpdatePasswordRequest(
        String email,
        String code,
        String newPassword
) {
}
