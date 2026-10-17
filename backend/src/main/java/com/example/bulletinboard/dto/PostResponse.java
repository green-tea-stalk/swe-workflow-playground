package com.example.bulletinboard.dto;

import com.example.bulletinboard.entity.PostEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Output payload DTO representing a single bulletin board post.
 *
 * @param id        the unique identifier of the post
 * @param name      the contributor display name
 * @param email     optional public email address (or null if omitted)
 * @param title     the message title
 * @param message   the message body content
 * @param createdAt the ISO 8601 UTC formatted submission timestamp
 * @param replies   associated chronological replies (guaranteed non-null, [] on zero replies)
 */
@Serdeable
public record PostResponse(
        Long id,
        String name,
        @Nullable String email,
        String title,
        String message,
        @JsonProperty("created_at") String createdAt,
        @JsonInclude(JsonInclude.Include.ALWAYS) List<ReplyResponse> replies) {

    /**
     * Compact constructor enforcing contract invariants, collection absence safety, and defensive immutability.
     */
    public PostResponse {
        replies = replies != null ? List.copyOf(replies) : List.of();
    }

    /**
     * Convenience constructor creating a PostResponse with an empty replies list.
     *
     * @param id        the unique identifier of the post
     * @param name      the contributor display name
     * @param email     optional public email address (or null if omitted)
     * @param title     the message title
     * @param message   the message body content
     * @param createdAt the ISO 8601 UTC formatted submission timestamp
     */
    public PostResponse(Long id, String name, String email, String title, String message, String createdAt) {
        this(id, name, email, title, message, createdAt, List.of());
    }

    private static final java.time.format.DateTimeFormatter UTC_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Converts a domain {@link PostEntity} to a {@link PostResponse} DTO with empty replies.
     *
     * @param entity the persisted post entity
     * @return the formatted response DTO with ISO 8601 UTC timestamp and empty replies list
     */
    public static PostResponse fromEntity(PostEntity entity) {
        return fromEntity(entity, List.of());
    }

    /**
     * Converts a domain {@link PostEntity} and associated replies to a {@link PostResponse} DTO.
     *
     * @param entity  the persisted post entity
     * @param replies the list of associated replies
     * @return the formatted response DTO with ISO 8601 UTC timestamp and replies list
     */
    public static PostResponse fromEntity(PostEntity entity, List<ReplyResponse> replies) {
        String formattedTimestamp = entity.createdAt() != null
                ? entity.createdAt().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER)
                : null;

        return new PostResponse(
                entity.id(),
                entity.name(),
                entity.email(),
                entity.title(),
                entity.message(),
                formattedTimestamp,
                replies);
    }
}
