package com.deepsleep.api.enums;

import java.util.Arrays;

public enum CourseStatus {
    CLOSED(0, "未开课"),
    OPEN(1, "开课"),
    UNKNOWN(-1, "未知");

    private final int code;
    private final String label;

    CourseStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static CourseStatus of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
