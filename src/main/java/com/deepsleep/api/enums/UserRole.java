package com.deepsleep.api.enums;

import java.util.Arrays;

public enum UserRole {
    ADMIN(0, "管理员"),
    TEACHER(1, "教师"),
    STUDENT(2, "学生"),
    UNKNOWN(-1, "未知");

    private final int code;
    private final String label;

    UserRole(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static UserRole of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(role -> role.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
