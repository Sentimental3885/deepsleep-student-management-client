package com.deepsleep.api.vo;

public record LoginVO(
        String token,
        String name,
        String avatar,
        Integer role
) {
}
