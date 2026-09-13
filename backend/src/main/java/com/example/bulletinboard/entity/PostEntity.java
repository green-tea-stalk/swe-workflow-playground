package com.example.bulletinboard.entity;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;

/**
 * Database entity representing a bulletin board post in the posts table.
 *
 * @param id        the unique auto-incremented primary key, or null if unpersisted
 * @param name      the contributor name (1–50 characters)
 * @param email     optional public email address (nullable)
 * @param title     the message title (1–100 characters)
 * @param message   the message body content (1–4,000 characters)
 * @param createdAt creation timestamp with microsecond resolution
 */
@MappedEntity("posts")
public record PostEntity(
        @Id
        @GeneratedValue
        @Nullable
        Long id,

        String name,

        @Nullable
        String email,

        String title,

        String message,

        @MappedProperty("created_at")
        LocalDateTime createdAt
) {}
