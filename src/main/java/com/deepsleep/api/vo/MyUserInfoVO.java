package com.deepsleep.api.vo;

import java.time.LocalDateTime;

public record MyUserInfoVO(
        Long id,
        String username,
        String name,
        String avatar,
        String phone,
        String email,
        Integer gender,
        Integer role,
        LocalDateTime createTime
) {
}
