package com.deepsleep.api.enums;

public enum SelectionStatus {
    PICKED(1, "已选"),
    DROPPED(2, "已退选"),
    OVER(3, "已结课"),
    UNKNOWN(-1, "未知");

    private final int code;
    private final String label;

    SelectionStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static SelectionStatus of(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return java.util.Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
