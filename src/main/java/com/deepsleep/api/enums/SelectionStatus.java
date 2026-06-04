package com.deepsleep.api.enums;

public enum SelectionStatus {
    PICKED("已选"),
    DROPPED("已退选"),
    OVER("已结课"),
    UNKNOWN("未知");

    private final String label;

    SelectionStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static SelectionStatus of(String name) {
        if (name == null || name.isBlank()) {
            return UNKNOWN;
        }
        try {
            return SelectionStatus.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}
