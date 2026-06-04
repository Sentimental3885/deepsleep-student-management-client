package com.deepsleep.api.dto.admin;

public record UpdateAdminUserRequest(
        String name,
        Integer gender,
        String phone,
        String email
) {
}
