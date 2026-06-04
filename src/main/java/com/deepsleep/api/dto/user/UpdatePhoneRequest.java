package com.deepsleep.api.dto.user;

public record UpdatePhoneRequest(
        String phone,
        String code
) {
}
