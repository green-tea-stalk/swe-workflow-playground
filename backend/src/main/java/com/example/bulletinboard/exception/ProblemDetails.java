package com.example.bulletinboard.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * RFC 9457 (Problem Details for HTTP APIs) に準拠したエラー応答 DTO。
 *
 * @param type 問題タイプを特定するURI参照
 * @param title 人間が読める問題の概要要約
 * @param status HTTPステータスコード
 * @param detail 発生した問題の個別具体的な説明
 * @param instance 問題が発生したリソースのURI参照
 * @param invalidParams 個別の入力検証エラー一覧（該当する場合のみ）
 */
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetails(
        @NotNull String type,
        @NotNull String title,
        int status,
        @NotNull String detail,
        @NotNull String instance,
        @Nullable @JsonProperty("invalid_params") List<InvalidParam> invalidParams
) {
    /**
     * コンパクトコンストラクタによる必須フィールドの null 検証。
     */
    public ProblemDetails {
        Objects.requireNonNull(type, "typeは非nullでなければなりません");
        Objects.requireNonNull(title, "titleは非nullでなければなりません");
        Objects.requireNonNull(detail, "detailは非nullでなければなりません");
        Objects.requireNonNull(instance, "instanceは非nullでなければなりません");
    }
}
