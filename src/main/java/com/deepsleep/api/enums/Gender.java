package com.deepsleep.api.enums;

import java.util.Arrays;

public enum Gender {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String label;

    Gender(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Gender of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(gender -> gender.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
