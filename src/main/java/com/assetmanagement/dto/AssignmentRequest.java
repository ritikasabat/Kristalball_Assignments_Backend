package com.assetmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AssignmentRequest(
        @NotNull Long assetId,
        @NotNull Long baseId,
        @NotBlank String personnelName,
        @NotNull @Min(1) Integer quantity,
        Instant assignedAt,
        String remarks
) {
}
