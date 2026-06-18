package com.deepsleep.api.vo;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record ScoreVO(
        Long id,
        String name,
        String teacherName,
        String teacherAvatar,
        String code,
        String semester,
        BigDecimal credit,
        Integer status,
        BigDecimal score,
        @JsonAlias("GPA")
        BigDecimal gpa,
        BigDecimal maxScore,
        BigDecimal minScore,
        Integer total,
        Integer rank,
        Integer denseRank
) {
}
