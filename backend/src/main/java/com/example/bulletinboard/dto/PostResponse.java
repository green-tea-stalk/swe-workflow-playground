package com.example.bulletinboard.dto;

import com.example.bulletinboard.entity.PostEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

import java.time.ZoneOffset;

/**
 * Output payload DTO representing a single bulletin board post.
 *
 * @param id        the unique identifier of the post
 * @param name      the contributor display name
 * @param email     optional public email address (or null if omitted)
 * @param title     the message title
 * @param message   the message body content
 * @param createdAt the ISO 8601 UTC formatted submission timestamp
 */
@Serdeable
public record PostResponse(
        Long id,
        String name,
        @Nullable
        String email,
        String title,
        String message,
        @JsonProperty("created_at")
        String createdAt
) {
    private static final java.time.format.DateTimeFormatter UTC_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Converts a domain {@link PostEntity} to a {@link PostResponse} DTO.
     *
     * @param entity the persisted post entity
     * @return the formatted response DTO with ISO 8601 UTC timestamp
     */
    public static PostResponse fromEntity(PostEntity entity) {
        String formattedTimestamp = entity.createdAt() != null
                ? entity.createdAt().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER)
                : null;

        return new PostResponse(
                entity.id(),
                entity.name(),
                entity.email(),
                entity.title(),
                entity.message(),
                formattedTimestamp
        );
    }
}
