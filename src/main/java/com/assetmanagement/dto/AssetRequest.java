package com.assetmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        String assetCode,
        @NotBlank String name,
        @NotBlank String equipmentType,
        @NotNull @Min(0) Integer quantity,
        @NotNull Long baseId,
        String status
) {
}
