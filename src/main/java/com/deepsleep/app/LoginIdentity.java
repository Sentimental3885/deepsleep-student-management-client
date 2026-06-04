package com.deepsleep.app;

import com.deepsleep.api.enums.UserRole;

public record LoginIdentity(
        String name,
        UserRole role
) {
}
