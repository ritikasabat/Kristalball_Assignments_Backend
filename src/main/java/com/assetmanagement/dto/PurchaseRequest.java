package com.assetmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        @NotNull Long baseId,
        @NotBlank String equipmentType,
        @NotBlank String assetName,
        @NotNull @Min(1) Integer quantity,
        @NotNull LocalDate purchaseDate,
        BigDecimal cost,
        String supplier,
        String description
) {
}
