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
    @DisplayName("PagedPostResponse に null の items を渡した場合でも、空リスト [] を保証して保持すること")
    void testPagedPostResponseGuaranteesEmptyListOnNull() {
        PagedPostResponse response = new PagedPostResponse(null, 0, 50, 0, 0);

        assertNotNull(response.items(), "items は決してnullであってはならない");
        assertTrue(response.items().isEmpty(), "nullが渡された場合は空リストでなければならない");
        assertEquals(0, response.page());
        assertEquals(50, response.size());
        assertEquals(0, response.totalItems());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName("PostResponse.fromEntity はエンティティのフィールドを正確にISO 8601 UTCタイムスタンプでDTOへマッピングすること")
    void testPostResponseFromEntityMapping() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 11, 12, 0, 0);
        PostEntity entity = new PostEntity(1L, "Alice", "alice@example.com", "Title", "Message", now);

        PostResponse response = PostResponse.fromEntity(entity);

        assertEquals(1L, response.id());
        assertEquals("Alice", response.name());
        assertEquals("alice@example.com", response.email());
        assertEquals("Title", response.title());
        assertEquals("Message", response.message());
        assertEquals("2026-09-11T12:00:00Z", response.createdAt(), "タイムスタンプはISO 8601 UTC形式（末尾Z）でなければならない");
    }

    @Test
    @DisplayName("PostResponse.fromEntity はメールアドレスが null の場合でも正常に null を許容してマッピングすること")
    void testPostResponseFromEntityWithNullEmail() {
        PostEntity entity = new PostEntity(2L, "Bob", null, "Title", "Message", LocalDateTime.now());

        PostResponse response = PostResponse.fromEntity(entity);

        assertEquals(2L, response.id());
        assertNull(response.email(), "メールアドレスが未入力の場合はnullでなければならない");
    }

    @Test
    @DisplayName("PagedPostResponse を JSON シリアライズした際、snake_case キーと空配列 [] が保証されること")
    void testPagedPostResponseJsonSerialization() throws IOException {
        PagedPostResponse response = new PagedPostResponse(List.of(), 0, 50, 0, 0);
        String json = jsonMapper.writeValueAsString(response);
        String compactJson = json.replaceAll("\\s+", "");

        assertAll(
                () -> assertTrue(compactJson.contains("\"items\":[]"), "items は JSON 上で空配列 [] としてシリアライズされること: " + json),
                () -> assertTrue(compactJson.contains("\"total_items\":0"), "total_items は snake_case でシリアライズされること"),
                () -> assertTrue(compactJson.contains("\"total_pages\":0"), "total_pages は snake_case でシリアライズされること")
        );
    }

    @Test
    @DisplayName("PostResponse を JSON シリアライズした際、created_at が snake_case で出力されること")
    void testPostResponseJsonSerialization() throws IOException {
        PostResponse post = new PostResponse(10L, "Charlie", null, "Title", "Body", "2026-09-11T12:00:00Z");
        String json = jsonMapper.writeValueAsString(post);

        assertAll(
                () -> assertTrue(json.contains("\"created_at\":\"2026-09-11T12:00:00Z\""), "created_at は snake_case で出力されること"),
                () -> assertTrue(json.contains("\"name\":\"Charlie\"")),
                () -> assertTrue(json.contains("\"id\":10"))
        );
    }

    @Test
    @DisplayName("JSON 文字列から PagedPostResponse および PostResponse が正確にデシリアライズされること")
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
        assertEquals(1L, paged.totalItems(), "total_items がマッピングされること");
        assertEquals(1, paged.totalPages(), "total_pages がマッピングされること");

        PostResponse item = paged.items().get(0);
        assertEquals(1L, item.id());
        assertEquals("David", item.name());
        assertEquals("2026-09-11T10:00:00Z", item.createdAt(), "created_at がマッピングされること");
    }

    @Test
    @DisplayName("JSON 文字列で items プロパティが省略されている場合でも、空リスト [] にフォールバックすること")
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
        assertNotNull(paged.items(), "items は決してnullであってはならない");
        assertTrue(paged.items().isEmpty(), "省略時は空リスト [] でなければならない");
    }
}
