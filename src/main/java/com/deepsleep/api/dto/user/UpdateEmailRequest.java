package com.deepsleep.api.dto.user;

public record UpdateEmailRequest(
        String email,
        String code
) {
}
