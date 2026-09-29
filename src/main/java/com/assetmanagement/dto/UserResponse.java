package com.assetmanagement.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        Long baseId,
        String baseName
) {
}
