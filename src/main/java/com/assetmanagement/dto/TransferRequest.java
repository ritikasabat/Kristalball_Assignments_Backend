package com.assetmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TransferRequest(
        @NotNull Long fromBaseId,
        @NotNull Long toBaseId,
        @NotNull Long assetId,
        @NotNull @Min(1) Integer quantity,
        Instant transferDate,
        String remarks
) {
}
