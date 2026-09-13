package com.example.bulletinboard.dto;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        @NotBlank(message = "Name must not be blank")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Name must not contain only whitespace")
        String name,

        @Nullable
        @Size(max = 254, message = "Email must not exceed 254 characters")
        @Email(message = "Email must be a well-formed email address")
        String email,

        @NotBlank(message = "Title must not be blank")
        @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Title must not contain only whitespace")
        String title,

        @NotBlank(message = "Message must not be blank")
        @Size(min = 1, max = 4000, message = "Message must be between 1 and 4000 characters")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Message must not contain only whitespace")
        String message
) {}
