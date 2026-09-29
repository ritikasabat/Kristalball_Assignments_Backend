package com.assetmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record BaseRequest(
        @NotBlank String name,
        String location
) {
}
