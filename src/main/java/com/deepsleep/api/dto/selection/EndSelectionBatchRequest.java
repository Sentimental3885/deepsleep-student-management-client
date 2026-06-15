package com.deepsleep.api.dto.selection;

import java.math.BigDecimal;
import java.util.List;

public record EndSelectionBatchRequest(
        Long courseId,
        List<Item> items
) {

    public record Item(
            Long studentId,
            BigDecimal score
    ) {
    }
}
