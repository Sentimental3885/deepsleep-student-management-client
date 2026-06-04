package com.deepsleep.api.vo;

import java.time.LocalDateTime;

public record GradeAnalysisVO(
        Long id,
        String content,
        LocalDateTime createTime
) {
}
