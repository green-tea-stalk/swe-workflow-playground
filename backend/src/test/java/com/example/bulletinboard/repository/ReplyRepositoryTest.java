package com.example.bulletinboard.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.entity.ReplyEntity;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test suite for {@link ReplyRepository} verifying database persistence contracts against MySQL 8.4.
 */
@MicronautTest(transactional = false)
@DisplayName("Integration test suite for ReplyRepository")
class ReplyRepositoryTest {

    @Inject
    PostRepository postRepository;

    @Inject
    ReplyRepository replyRepository;

    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        replyRepository.deleteAll();
        postRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        testPost = postRepository.save(new PostEntity(null, "投稿者", "author@example.com", "テスト親投稿", "親投稿の本文です。", now));
    }

    @AfterEach
    void tearDown() {
        replyRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("Saving a new reply should auto-generate primary key ID, persist entity, and allow retrieval")
    void testSaveReplySuccessfully() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ReplyEntity unsaved = new ReplyEntity(null, testPost.id(), "返信者1", "reply1@example.com", "返信メッセージ本文です。", now);

        ReplyEntity saved = replyRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id(), "Saved reply ID must not be null"),
                () -> assertTrue(saved.id() > 0, "Saved reply ID must be positive"),
                () -> assertEquals(testPost.id(), saved.postId(), "Post ID must match parent post"),
                () -> assertEquals("返信者1", saved.name(), "Name must match"),
                () -> assertEquals("reply1@example.com", saved.email(), "Email must match"),
                () -> assertEquals("返信メッセージ本文です。", saved.message(), "Message must match"),
                () -> assertEquals(
                        now, saved.createdAt().truncatedTo(ChronoUnit.SECONDS), "Creation timestamp must match"));

        Optional<ReplyEntity> retrieved = replyRepository.findById(saved.id());
        assertTrue(retrieved.isPresent(), "Must be retrievable from database by ID");
        assertEquals(saved.id(), retrieved.get().id());
        assertEquals(saved.message(), retrieved.get().message(), "Retrieved message must match saved message");
    }

    @Test
    @DisplayName("Saving a reply with null email should succeed and allow retrieval")
    void testSaveReplyWithNullEmail() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ReplyEntity unsaved = new ReplyEntity(null, testPost.id(), "匿名返信者", null, "メールなしの返信です。", now);

        ReplyEntity saved = replyRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id(), "Saved ID must not be null"),
                () -> assertEquals("匿名返信者", saved.name()),
                () -> assertNull(saved.email(), "Email must be null"),
                () -> assertEquals("メールなしの返信です。", saved.message()));
    }

    @Test
    @DisplayName("Deleting parent post should cascade delete all associated replies")
    void testCascadeDeleteWhenParentPostDeleted() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        replyRepository.save(new ReplyEntity(null, testPost.id(), "返信1", null, "本文1", now));
        replyRepository.save(new ReplyEntity(null, testPost.id(), "返信2", null, "本文2", now.plusSeconds(1)));

        assertEquals(
                2,
                replyRepository.findByPostIdOrderByCreatedAtAsc(testPost.id()).size());

        postRepository.deleteById(testPost.id());

        List<ReplyEntity> remainingReplies = replyRepository.findByPostIdOrderByCreatedAtAsc(testPost.id());
        assertTrue(remainingReplies.isEmpty(), "Replies must be cascade-deleted when parent post is removed");
        assertEquals(0, replyRepository.count(), "Total reply count in table must be 0 after cascade deletion");
    }

    @Test
    @DisplayName("Replies must be retrieved in chronological order (ascending by createdAt)")
    void testFindByPostIdOrderByCreatedAtAsc() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 14, 12, 0, 0);

        ReplyEntity reply1 = replyRepository.save(new ReplyEntity(null, testPost.id(), "返信1", null, "最古の返信", baseTime));
        ReplyEntity reply2 = replyRepository.save(
                new ReplyEntity(null, testPost.id(), "返信2", null, "中間の返信", baseTime.plusMinutes(5)));
        ReplyEntity reply3 = replyRepository.save(
                new ReplyEntity(null, testPost.id(), "返信3", null, "最新の返信", baseTime.plusMinutes(10)));

        List<ReplyEntity> replies = replyRepository.findByPostIdOrderByCreatedAtAsc(testPost.id());

        assertAll(
                () -> assertEquals(3, replies.size(), "Must retrieve 3 replies"),
                () -> assertEquals(reply1.id(), replies.get(0).id(), "First reply must be the oldest"),
                () -> assertEquals(reply2.id(), replies.get(1).id(), "Second reply must be the middle"),
                () -> assertEquals(reply3.id(), replies.get(2).id(), "Third reply must be the newest"));
    }

    @Test
    @DisplayName(
            "Batch query findByPostIdInOrderByCreatedAtAsc must retrieve all replies for requested posts in chronological order")
    void testFindByPostIdInOrderByCreatedAtAsc() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 9, 14, 12, 0, 0);
        PostEntity secondPost = postRepository.save(new PostEntity(null, "第2投稿者", null, "親投稿2", "本文2", baseTime));

        ReplyEntity p1r1 = replyRepository.save(
                new ReplyEntity(null, testPost.id(), "P1R1", null, "P1-R1", baseTime.plusMinutes(1)));
        ReplyEntity p2r1 = replyRepository.save(
                new ReplyEntity(null, secondPost.id(), "P2R1", null, "P2-R1", baseTime.plusMinutes(2)));
        ReplyEntity p1r2 = replyRepository.save(
                new ReplyEntity(null, testPost.id(), "P1R2", null, "P1-R2", baseTime.plusMinutes(3)));

        List<ReplyEntity> batchReplies =
                replyRepository.findByPostIdInOrderByCreatedAtAsc(List.of(testPost.id(), secondPost.id()));

        assertAll(
                () -> assertEquals(3, batchReplies.size(), "Must retrieve all 3 replies across both posts"),
                () -> assertEquals(p1r1.id(), batchReplies.get(0).id()),
                () -> assertEquals(p2r1.id(), batchReplies.get(1).id()),
                () -> assertEquals(p1r2.id(), batchReplies.get(2).id()));
    }

    @Test
    @DisplayName("Defensive contract: should return empty list [] when post has no replies")
    void testGuaranteesEmptyListWhenNoReplies() {
        List<ReplyEntity> singleResult = replyRepository.findByPostIdOrderByCreatedAtAsc(testPost.id());
        assertNotNull(singleResult, "Result must not be null");
        assertTrue(singleResult.isEmpty(), "Result must be an empty list []");

        List<ReplyEntity> batchResult = replyRepository.findByPostIdInOrderByCreatedAtAsc(List.of(testPost.id()));
        assertNotNull(batchResult, "Batch result must not be null");
        assertTrue(batchResult.isEmpty(), "Batch result must be an empty list []");
    }

    @Test
    @DisplayName("Strings containing SQL injection payloads must be safely persisted and retrieved as literals")
    void testSqlInjectionSafety() {
        String sqlName = "'); DROP TABLE replies; --";
        String sqlMessage = "' OR '1'='1";

        ReplyEntity saved = replyRepository.save(new ReplyEntity(
                null,
                testPost.id(),
                sqlName,
                null,
                sqlMessage,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));

        Optional<ReplyEntity> retrieved = replyRepository.findById(saved.id());
        assertTrue(retrieved.isPresent());
        assertEquals(sqlName, retrieved.get().name());
        assertEquals(sqlMessage, retrieved.get().message());
    }

    @Test
    @DisplayName("Precondition violation: saving a reply with a non-existent post_id must throw DataAccessException")
    void testSaveReplyWithNonExistentPostIdThrowsException() {
        long nonExistentPostId = 999999L;
        ReplyEntity invalid = new ReplyEntity(null, nonExistentPostId, "返信者", null, "本文", LocalDateTime.now());

        assertThrows(
                DataAccessException.class,
                () -> replyRepository.save(invalid),
                "DataAccessException must be thrown due to foreign key constraint violation");
    }
}
