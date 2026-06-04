package com.deepsleep.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenStore {

    private String accessToken;

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    public void clear() {
        this.accessToken = null;
    }
}
