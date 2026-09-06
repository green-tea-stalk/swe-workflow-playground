package com.example.bulletinboard.dto;

import com.example.bulletinboard.entity.MessageEntity;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * 掲示板メッセージ応答リソース DTO。
 *
 * @param id メッセージ一意識別子
 * @param name 投稿者表示名
 * @param email 連絡先メールアドレス（未設定時はnull）
 * @param title 投稿タイトル
 * @param message 投稿本文
 * @param createdAt 投稿作成日時（UTC）
 */
@Serdeable
public record MessageResponse(
        @NotNull Long id,
        @NotNull String name,
        @Nullable String email,
        @NotNull String title,
        @NotNull String message,
        @NotNull Instant createdAt
) {
    /**
     * メッセージエンティティから応答DTOへ変換する。
     *
     * @param entity 変換対象のメッセージエンティティ（非null）
     * @return 変換されたメッセージ応答リソースDTO
     * @throws NullPointerException entityがnullの場合
     */
    public static MessageResponse fromEntity(MessageEntity entity) {
        java.util.Objects.requireNonNull(entity, "entityは非nullでなければなりません");
        return new MessageResponse(
                entity.id(),
                entity.name(),
                entity.email(),
                entity.title(),
                entity.message(),
                entity.createdAt()
        );
    }
}

