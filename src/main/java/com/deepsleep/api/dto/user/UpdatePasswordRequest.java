package com.deepsleep.api.dto.user;

public record UpdatePasswordRequest(
        String code,
        String newPassword
) {
}
