package com.example.bulletinboard.dto;

import com.example.bulletinboard.entity.PostEntity;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying DTO construction, serialization invariants, and empty collection guarantees.
 */
@MicronautTest
class DtoContractTest {

    @Inject
    JsonMapper jsonMapper;

    @Test
    @DisplayName("PagedPostResponse should guarantee non-null empty list when items is null")
    void testPagedPostResponseGuaranteesEmptyListOnNull() {
        PagedPostResponse response = new PagedPostResponse(null, 0, 50, 0, 0);

        assertNotNull(response.items(), "items must never be null");
        assertTrue(response.items().isEmpty(), "items must be empty list [] when null is supplied");
        assertEquals(0, response.page());
        assertEquals(50, response.size());
        assertEquals(0, response.totalItems());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName("PostResponse.fromEntity should accurately map entity fields with ISO 8601 UTC timestamp")
    void testPostResponseFromEntityMapping() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 11, 12, 0, 0);
        PostEntity entity = new PostEntity(1L, "Alice", "alice@example.com", "Title", "Message", now);

        PostResponse response = PostResponse.fromEntity(entity);

        assertEquals(1L, response.id());
        assertEquals("Alice", response.name());
        assertEquals("alice@example.com", response.email());
        assertEquals("Title", response.title());
        assertEquals("Message", response.message());
        assertEquals("2026-09-11T12:00:00Z", response.createdAt(), "Timestamp must be in ISO 8601 UTC format ending with Z");
    }

    @Test
    @DisplayName("PostResponse.fromEntity should accept and retain null email")
    void testPostResponseFromEntityWithNullEmail() {
        PostEntity entity = new PostEntity(2L, "Bob", null, "Title", "Message", LocalDateTime.now());

        PostResponse response = PostResponse.fromEntity(entity);

        assertEquals(2L, response.id());
        assertNull(response.email(), "email must be null when entity email is omitted");
    }

    @Test
    @DisplayName("PagedPostResponse JSON serialization should guarantee snake_case keys and empty array []")
    void testPagedPostResponseJsonSerialization() throws IOException {
        PagedPostResponse response = new PagedPostResponse(List.of(), 0, 50, 0, 0);
        String json = jsonMapper.writeValueAsString(response);
        String compactJson = json.replaceAll("\\s+", "");

        assertAll(
                () -> assertTrue(compactJson.contains("\"items\":[]"), "items must be serialized as empty array [] in JSON: " + json),
                () -> assertTrue(compactJson.contains("\"total_items\":0"), "total_items must be serialized in snake_case"),
                () -> assertTrue(compactJson.contains("\"total_pages\":0"), "total_pages must be serialized in snake_case")
        );
    }

    @Test
    @DisplayName("PostResponse JSON serialization should output created_at in snake_case")
    void testPostResponseJsonSerialization() throws IOException {
        PostResponse post = new PostResponse(10L, "Charlie", null, "Title", "Body", "2026-09-11T12:00:00Z");
        String json = jsonMapper.writeValueAsString(post);

        assertAll(
                () -> assertTrue(json.contains("\"created_at\":\"2026-09-11T12:00:00Z\""), "created_at must be serialized in snake_case"),
                () -> assertTrue(json.contains("\"name\":\"Charlie\"")),
                () -> assertTrue(json.contains("\"id\":10"))
        );
    }

    @Test
    @DisplayName("JSON string should accurately deserialize into PagedPostResponse and PostResponse")
    void testJsonDeserialization() throws IOException {
        String json = """
                {
                    "items": [
                        {
                            "id": 1,
                            "name": "David",
                            "email": "david@example.com",
                            "title": "Welcome",
                            "message": "Hello",
                            "created_at": "2026-09-11T10:00:00Z"
                        }
                    ],
                    "page": 0,
                    "size": 50,
                    "total_items": 1,
                    "total_pages": 1
                }
                """;

        PagedPostResponse paged = jsonMapper.readValue(json, PagedPostResponse.class);

        assertNotNull(paged);
        assertEquals(1, paged.items().size());
        assertEquals(1L, paged.totalItems(), "total_items must be mapped properly");
        assertEquals(1, paged.totalPages(), "total_pages must be mapped properly");

        PostResponse item = paged.items().get(0);
        assertEquals(1L, item.id());
        assertEquals("David", item.name());
        assertEquals("2026-09-11T10:00:00Z", item.createdAt(), "created_at must be mapped properly");
    }

    @Test
    @DisplayName("JSON string with omitted items property should fall back to empty list []")
    void testJsonDeserializationWithMissingItemsDefaultsToEmptyList() throws IOException {
        String json = """
                {
                    "page": 0,
                    "size": 50,
                    "total_items": 0,
                    "total_pages": 0
                }
                """;

        PagedPostResponse paged = jsonMapper.readValue(json, PagedPostResponse.class);

        assertNotNull(paged);
        assertNotNull(paged.items(), "items must never be null");
        assertTrue(paged.items().isEmpty(), "items must default to empty list [] when omitted");
    }
}
