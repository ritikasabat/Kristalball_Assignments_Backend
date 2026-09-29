package com.assetmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ExpenditureRequest(
        @NotNull Long assetId,
        @NotNull Long baseId,
        @NotNull @Min(1) Integer quantity,
        String reason,
        Instant expendedAt
) {
}
