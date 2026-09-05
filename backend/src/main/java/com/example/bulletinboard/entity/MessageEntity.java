package com.example.bulletinboard.entity;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 掲示板メッセージの永続化エンティティ。
 * Micronaut Data JDBC の不変レコードマッピングとして機能する。
 *
 * @param id 自動採番される主キーID（永続化前はnullを許容）
 * @param name 投稿者表示名（1〜50文字、必須）
 * @param email 連絡先メールアドレス（最大100文字、任意）
 * @param title 投稿タイトル（1〜100文字、必須）
 * @param message 投稿本文（1〜1000文字、必須）
 * @param createdAt サーバー側で採番・付与される投稿日時
 */
@Serdeable
@MappedEntity("messages")
public record MessageEntity(
        @Id
        @GeneratedValue
        @Nullable
        Long id,

        @NotBlank
        @Size(max = 50)
        String name,

        @Nullable
        @Email
        @Size(max = 100)
        String email,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @Size(max = 1000)
        String message,

        @NotNull
        @MappedProperty("created_at")
        Instant createdAt
) {
}

