package com.assetmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Update payload for {@code PUT /api/users/{id}}. Password is optional:
 * leaving it blank keeps the existing BCrypt hash unchanged.
 */
public record UserUpdateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String password,
        @NotBlank String role,
        Long baseId
) {
}
