package com.deepsleep.api.result;

import java.util.List;

public record PageResult<T>(
        List<T> records,
        Long total,
        Long size,
        Long current,
        Long pages
) {
}
