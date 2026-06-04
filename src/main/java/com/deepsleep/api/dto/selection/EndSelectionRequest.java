package com.deepsleep.api.dto.selection;

import java.math.BigDecimal;

public record EndSelectionRequest(
        Long sid,
        Long cid,
        BigDecimal score
) {
}
