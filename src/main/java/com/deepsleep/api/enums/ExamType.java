package com.deepsleep.api.enums;

import java.util.Arrays;

public enum ExamType {
    MIDTERM(1, "期中"),
    FINAL(2, "期末"),
    MAKEUP(3, "补考"),
    UNKNOWN(-1, "未知");

    private final int code;
    private final String label;

    ExamType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static ExamType of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
