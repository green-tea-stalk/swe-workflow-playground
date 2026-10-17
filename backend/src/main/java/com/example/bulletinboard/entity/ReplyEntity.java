package com.example.bulletinboard.entity;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.LocalDateTime;

/**
 * Database entity representing a bulletin board reply in the replies table.
 *
 * @param id        the unique auto-incremented primary key, or null if unpersisted
 * @param postId    the parent post ID foreign key reference
 * @param name      the contributor display name (1–50 characters)
 * @param email     optional public email address (nullable)
 * @param message   the reply message body content (1–4,000 characters)
 * @param createdAt creation timestamp with microsecond resolution
 */
@MappedEntity("replies")
public record ReplyEntity(
        @Id @GeneratedValue @Nullable Long id,
        @MappedProperty("post_id") Long postId,
        String name,
        @Nullable String email,
        String message,
        @MappedProperty("created_at") LocalDateTime createdAt) {}
