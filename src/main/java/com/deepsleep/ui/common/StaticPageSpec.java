package com.deepsleep.ui.common;

import java.util.List;

public record StaticPageSpec(
        String title,
        String subtitle,
        List<String> stats,
        List<String> columns,
        List<List<String>> rows,
        List<String> formFields,
        String content
) {
}
