package com.assetmanagement.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        String role,
        Long baseId,
        String baseName
) {
    public AuthResponse(String token, Long userId, String name, String email, String role, Long baseId, String baseName) {
        this(token, "Bearer", userId, name, email, role, baseId, baseName);
    }
}
