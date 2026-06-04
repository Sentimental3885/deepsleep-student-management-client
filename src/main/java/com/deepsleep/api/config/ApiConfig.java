package com.deepsleep.api.config;

import java.time.Duration;

public record ApiConfig(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Duration writeTimeout
) {

    public static ApiConfig of(String baseUrl) {
        return new ApiConfig(
                baseUrl,
                Duration.ofSeconds(10),
                Duration.ofSeconds(20),
                Duration.ofSeconds(20)
        );
    }
}
