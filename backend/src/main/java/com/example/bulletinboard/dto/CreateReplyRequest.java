package com.example.bulletinboard.dto;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input request payload DTO for submitting a reply to an existing post.
 *
 * @param name    the contributor name (required, 1–50 characters, non-blank)
 * @param email   optional contributor email address (max 254 characters)
 * @param message the reply body content (required, 1–4,000 characters, non-blank)
 */
@Serdeable
public record CreateReplyRequest(
        @NotBlank(message = "{validation.name.required}") @Size(min = 1, max = 50, message = "{validation.name.size}")
                String name,
        @Nullable @Size(max = 254, message = "{validation.email.size}") @Email(message = "{validation.email.format}")
                String email,
        @NotBlank(message = "{validation.message.required}")
                @Size(min = 1, max = 4000, message = "{validation.message.size}")
                String message) {}
