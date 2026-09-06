package com.example.bulletinboard.dto;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 掲示板新規メッセージ投稿リクエスト DTO。
 *
 * @param name 投稿者表示名（1〜50文字、空白のみ不可）
 * @param email 連絡先メールアドレス（任意、最大100文字）
 * @param title 投稿タイトル（1〜100文字、空白のみ不可）
 * @param message 投稿本文（1〜1000文字、空白のみ不可）
 */
@Serdeable
public record MessageCreateRequest(
        @NotBlank(message = "名前は必須であり、空白のみでは指定できません")
        @Size(min = 1, max = 50, message = "名前は1文字以上50文字以下で指定してください")
        String name,

        @Nullable
        @Email(message = "メールアドレスの形式が正しくありません")
        @Size(max = 100, message = "メールアドレスは最大100文字です")
        String email,

        @NotBlank(message = "タイトルは必須であり、空白のみでは指定できません")
        @Size(min = 1, max = 100, message = "タイトルは1文字以上100文字以下で指定してください")
        String title,

        @NotBlank(message = "メッセージ本文は必須であり、空白のみでは指定できません")
        @Size(min = 1, max = 1000, message = "メッセージ本文は1文字以上1000文字以下で指定してください")
        String message
) {
    /**
     * コンパクトコンストラクタによる全入力値の事前正規化。
     * 各フィールドの前後の空白を除去し、メールアドレスが空文字列または空白文字のみの場合はnullに変換する。
     */
    public MessageCreateRequest {
        name = name != null ? name.trim() : null;
        if (email != null) {
            String trimmed = email.trim();
            email = trimmed.isEmpty() ? null : trimmed;
        }
        title = title != null ? title.trim() : null;
        message = message != null ? message.trim() : null;
    }
}
