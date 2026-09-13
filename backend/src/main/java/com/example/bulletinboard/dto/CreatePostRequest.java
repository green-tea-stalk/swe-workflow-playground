package com.example.bulletinboard.dto;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input request payload DTO for submitting a new post.
 *
 * @param name    the contributor name (required, 1–50 characters, non-blank)
 * @param email   optional contributor email address (max 254 characters)
 * @param title   the message title (required, 1–100 characters, non-blank)
 * @param message the message body content (required, 1–4,000 characters, non-blank)
 */
@Serdeable
public record CreatePostRequest(
        @NotBlank(message = "{validation.name.required}")
        @Size(min = 1, max = 50, message = "{validation.name.size}")
        String name,

        @Nullable
        @Size(max = 254, message = "{validation.email.size}")
        @Email(message = "{validation.email.format}")
        String email,

        @NotBlank(message = "{validation.title.required}")
        @Size(min = 1, max = 100, message = "{validation.title.size}")
        String title,

        @NotBlank(message = "{validation.message.required}")
        @Size(min = 1, max = 4000, message = "{validation.message.size}")
        String message
) {}

