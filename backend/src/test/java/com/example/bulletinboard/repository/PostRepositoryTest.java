package com.example.bulletinboard.repository;

import com.example.bulletinboard.entity.PostEntity;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test suite for {@link PostRepository} verifying database persistence contracts against MySQL 8.4.
 */
@MicronautTest(transactional = false)
@DisplayName("Integration test suite for PostRepository")
class PostRepositoryTest {

    @Inject
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("Saving a new post should auto-generate primary key ID, persist entity, and allow retrieval")
    void testSavePostSuccessfully() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PostEntity unsaved = new PostEntity(
                null,
                "山田 太郎",
                "yamada@example.com",
                "初回投稿",
                "メッセージ本文です。",
                now
        );

        PostEntity saved = postRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id(), "Saved ID must not be null"),
                () -> assertTrue(saved.id() > 0, "Saved ID must be positive"),
                () -> assertEquals("山田 太郎", saved.name(), "Name must match"),
                () -> assertEquals("yamada@example.com", saved.email(), "Email must match"),
                () -> assertEquals("初回投稿", saved.title(), "Title must match"),
                () -> assertEquals("メッセージ本文です。", saved.message(), "Message must match"),
                () -> assertEquals(now, saved.createdAt().truncatedTo(ChronoUnit.SECONDS), "Created timestamp must match")
        );

        Optional<PostEntity> retrieved = postRepository.findById(saved.id());
        assertTrue(retrieved.isPresent(), "Must be retrievable from database by ID");
        assertEquals(saved.id(), retrieved.get().id());
    }

    @Test
    @DisplayName("Saving a post with null email should succeed and allow retrieval")
    void testSavePostWithNullEmail() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        PostEntity unsaved = new PostEntity(
                null,
                "名無しさん",
                null,
                "匿名タイトル",
                "メールなしの投稿",
                now
        );

        PostEntity saved = postRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id(), "Saved ID must not be null"),
                () -> assertEquals("名無しさん", saved.name()),
                () -> assertNull(saved.email(), "Email must be null"),
                () -> assertEquals("匿名タイトル", saved.title()),
                () -> assertEquals("メールなしの投稿", saved.message())
        );
    }

    @Test
    @DisplayName("Strings containing SQL injection payloads must be safely persisted and retrieved as literals")
    void testSavePostWithSqlInjectionPayload() {
        String sqlInjectionTitle = "'); DROP TABLE posts; --";
        String sqlInjectionMessage = "' OR '1'='1";
        PostEntity unsaved = new PostEntity(
                null,
                "攻撃者テスト",
                "attacker@example.com",
                sqlInjectionTitle,
                sqlInjectionMessage,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        PostEntity saved = postRepository.save(unsaved);
        Optional<PostEntity> retrieved = postRepository.findById(saved.id());

        assertTrue(retrieved.isPresent(), "Record must be retrieved successfully");
        assertEquals(sqlInjectionTitle, retrieved.get().title(), "SQL injection string must be preserved as a literal");
        assertEquals(sqlInjectionMessage, retrieved.get().message(), "SQL injection string must be preserved as a literal");
    }

    @Test
    @DisplayName("Posts must be retrieved ordered descending by createdAt")
    void testFindAllOrderByCreatedAtDesc() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 11, 10, 0, 0);

        PostEntity first = postRepository.save(new PostEntity(
                null, "投稿者1", null, "タイトル1", "本文1", baseTime
        ));
        PostEntity second = postRepository.save(new PostEntity(
                null, "投稿者2", null, "タイトル2", "本文2", baseTime.plusHours(1)
        ));
        PostEntity third = postRepository.save(new PostEntity(
                null, "投稿者3", null, "タイトル3", "本文3", baseTime.plusHours(2)
        ));

        Pageable pageable = Pageable.from(0, 50, Sort.of(Sort.Order.desc("createdAt")));
        Page<PostEntity> page = postRepository.findAll(pageable);
        List<PostEntity> posts = page.getContent();

        assertAll(
                () -> assertEquals(3, page.getTotalSize(), "Total count must be 3"),
                () -> assertEquals(3, posts.size(), "Current page count must be 3"),
                () -> assertEquals(third.id(), posts.get(0).id(), "First element must be the newest post (third)"),
                () -> assertEquals(second.id(), posts.get(1).id(), "Second element must be the second newest post (second)"),
                () -> assertEquals(first.id(), posts.get(2).id(), "Last element must be the oldest post (first)")
        );
    }

    static Stream<Arguments> paginationTestCases() {
        return Stream.of(
                Arguments.of(0, 3, List.of("タイトル5", "タイトル4", "タイトル3")),
                Arguments.of(1, 2, List.of("タイトル2", "タイトル1")),
                Arguments.of(2, 0, List.of())
        );
    }

    @ParameterizedTest(name = "pagination test for page index {0} (expected size: {1})")
    @MethodSource("paginationTestCases")
    @DisplayName("Posts must be correctly paginated with specified page index and size")
    void testPaginatePostsCorrectly(int pageIndex, int expectedSize, List<String> expectedTitles) {
        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 11, 10, 0, 0);
        for (int i = 1; i <= 5; i++) {
            postRepository.save(new PostEntity(
                    null, "投稿者" + i, null, "タイトル" + i, "本文" + i, baseTime.plusMinutes(i)
            ));
        }

        int pageSize = 3;
        Pageable pageable = Pageable.from(pageIndex, pageSize, Sort.of(Sort.Order.desc("createdAt")));
        Page<PostEntity> page = postRepository.findAll(pageable);
        List<String> actualTitles = page.getContent().stream().map(PostEntity::title).toList();

        assertAll(
                () -> assertEquals(5, page.getTotalSize(), "Total size must be 5"),
                () -> assertEquals(2, page.getTotalPages(), "Total pages must be 2"),
                () -> assertEquals(expectedSize, page.getContent().size(), "Page content size must match expected size"),
                () -> assertEquals(expectedTitles, actualTitles, "Page titles and ordering must match expected")
        );
    }

    @Test
    @DisplayName("Defensive contract: should guarantee empty collection [] instead of null when no records exist")
    void testFindAllGuaranteesEmptyListWhenNoRecords() {
        Pageable pageable = Pageable.from(0, 50, Sort.of(Sort.Order.desc("createdAt")));
        Page<PostEntity> page = postRepository.findAll(pageable);

        assertNotNull(page, "Page result must not be null");
        assertNotNull(page.getContent(), "Content list must not be null");
        assertTrue(page.getContent().isEmpty(), "Must be empty collection [] when zero records exist");
        assertEquals(0, page.getTotalSize(), "Total record count must be 0");
    }

    @Test
    @DisplayName("Precondition violation: saving a post with null name must throw DataAccessException")
    void testSavePostWithNullNameThrowsException() {
        PostEntity invalid = new PostEntity(
                null,
                null,
                "test@example.com",
                "タイトル",
                "メッセージ本文",
                LocalDateTime.now()
        );

        assertThrows(DataAccessException.class, () -> postRepository.save(invalid),
                "DataAccessException must be thrown due to NOT NULL constraint violation");
    }
}
