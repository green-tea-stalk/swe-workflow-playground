package com.example.bulletinboard.exception;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * RFC 9457 Problem Details 内の個別入力検証エラー記述 DTO。
 *
 * @param name エラーが発生した入力フィールド名
 * @param reason 入力検証エラーの具体的な理由
 */
@Serdeable
public record InvalidParam(
        @NotNull String name,
        @NotNull String reason
) {
    /**
     * コンパクトコンストラクタによる防御的 null 検証。
     */
    public InvalidParam {
        Objects.requireNonNull(name, "nameは非nullでなければなりません");
        Objects.requireNonNull(reason, "reasonは非nullでなければなりません");
    }
}
