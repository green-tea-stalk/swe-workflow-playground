package com.example.bulletinboard.dto;

import com.example.bulletinboard.entity.ReplyEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import java.time.ZoneOffset;

/**
 * Output payload DTO representing a single bulletin board reply.
 *
 * @param id        the unique identifier of the reply
 * @param postId    the parent post ID
 * @param name      the contributor display name
 * @param email     optional public email address (or null if omitted)
 * @param message   the reply body content
 * @param createdAt the ISO 8601 UTC formatted submission timestamp
 */
@Serdeable
public record ReplyResponse(
        Long id,
        @JsonProperty("post_id") Long postId,
        String name,
        @Nullable String email,
        String message,
        @JsonProperty("created_at") String createdAt) {

    private static final java.time.format.DateTimeFormatter UTC_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Converts a domain {@link ReplyEntity} to a {@link ReplyResponse} DTO.
     *
     * @param entity the persisted reply entity
     * @return the formatted response DTO with ISO 8601 UTC timestamp
     */
    public static ReplyResponse fromEntity(ReplyEntity entity) {
        String formattedTimestamp = entity.createdAt() != null
                ? entity.createdAt().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER)
                : null;

        return new ReplyResponse(
                entity.id(), entity.postId(), entity.name(), entity.email(), entity.message(), formattedTimestamp);
    }
}
