package com.example.bulletinboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * Output payload DTO representing a paginated collection of posts.
 * Strictly guarantees that {@link #items()} is never null, never omitted in JSON, and defaults to an empty list {@code []}.
 * Protects against external collection modifications by wrapping with {@link List#copyOf(java.util.Collection)}.
 *
 * @param items      the list of posts on the current page (guaranteed non-null, empty list on zero results)
 * @param page       the current zero-based page index
 * @param size       the maximum page size limit
 * @param totalItems the total number of recorded posts
 * @param totalPages the total number of available pages
 */
@Serdeable
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PagedPostResponse(
        @JsonInclude(JsonInclude.Include.ALWAYS)
        List<PostResponse> items,
        int page,
        int size,
        @JsonProperty("total_items")
        long totalItems,
        @JsonProperty("total_pages")
        int totalPages
) {
    /**
     * Compact constructor enforcing contract invariants, collection absence safety, and defensive immutability.
     */
    public PagedPostResponse {
        items = (items == null) ? List.of() : List.copyOf(items);
    }
}
