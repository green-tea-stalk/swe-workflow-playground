package com.example.bulletinboard.dto;

import io.micronaut.data.model.Page;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * ページネーション共通封筒 DTO。
 *
 * @param content 現在ページのデータリスト
 * @param page 0開始の現在ページインデックス
 * @param size 1ページあたりの件数
 * @param totalElements 全ページにまたがる総件数
 * @param totalPages 総ページ数
 * @param <T> 要素の型
 */
@Serdeable
public record PageResponse<T>(
        @NotNull List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * Micronaut Data のページネーション結果から汎用ページネーション封筒DTOへ変換する。
     *
     * @param <E> エンティティ型
     * @param <T> DTO型
     * @param page Micronaut Data の Page オブジェクト（非null）
     * @param mapper エンティティからDTOへの変換関数（非null）
     * @return 変換されたページネーション封筒DTO
     * @throws NullPointerException pageまたはmapperがnullの場合
     */
    public static <E, T> PageResponse<T> fromPage(Page<E> page, Function<E, T> mapper) {
        java.util.Objects.requireNonNull(page, "pageは非nullでなければなりません");
        java.util.Objects.requireNonNull(mapper, "mapperは非nullでなければなりません");
        List<T> content = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
                content,
                page.getPageNumber(),
                page.getSize(),
                page.getTotalSize(),
                page.getTotalPages()
        );
    }
}

