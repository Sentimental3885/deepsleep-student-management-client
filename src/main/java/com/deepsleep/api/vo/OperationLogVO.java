package com.deepsleep.api.vo;

import java.time.LocalDateTime;

public record OperationLogVO(
        Long id,
        Long operatorId,
        String operatorName,
        String operation,
        String method,
        String params,
        Integer status,
        String errorMsg,
        LocalDateTime createTime
) {
}
