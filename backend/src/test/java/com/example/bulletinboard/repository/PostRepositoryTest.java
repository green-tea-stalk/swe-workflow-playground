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
@DisplayName("掲示板投稿リポジトリの統合テスト")
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
    @DisplayName("新規投稿を保存すると主キーIDが自動採番され、永続化および再取得できること")
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
                () -> assertNotNull(saved.id(), "保存後のIDはnullでないこと"),
                () -> assertTrue(saved.id() > 0, "保存後のIDは正の整数であること"),
                () -> assertEquals("山田 太郎", saved.name(), "名前が一致すること"),
                () -> assertEquals("yamada@example.com", saved.email(), "メールアドレスが一致すること"),
                () -> assertEquals("初回投稿", saved.title(), "タイトルが一致すること"),
                () -> assertEquals("メッセージ本文です。", saved.message(), "本文が一致すること"),
                () -> assertEquals(now, saved.createdAt().truncatedTo(ChronoUnit.SECONDS), "作成日時が一致すること")
        );

        Optional<PostEntity> retrieved = postRepository.findById(saved.id());
        assertTrue(retrieved.isPresent(), "データベースからIDで再取得できること");
        assertEquals(saved.id(), retrieved.get().id());
    }

    @Test
    @DisplayName("メールアドレスがnullの投稿も正常に保存および再取得できること")
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
                () -> assertNotNull(saved.id(), "保存後のIDはnullでないこと"),
                () -> assertEquals("名無しさん", saved.name()),
                () -> assertNull(saved.email(), "メールアドレスはnullであること"),
                () -> assertEquals("匿名タイトル", saved.title()),
                () -> assertEquals("メールなしの投稿", saved.message())
        );
    }

    @Test
    @DisplayName("SQLインジェクション構文を含む文字列がリテラルとして安全に保存および取得できること")
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

        assertTrue(retrieved.isPresent(), "レコードが正常に取得できること");
        assertEquals(sqlInjectionTitle, retrieved.get().title(), "SQLインジェクション文字列がリテラルとして保存されていること");
        assertEquals(sqlInjectionMessage, retrieved.get().message(), "SQLインジェクション文字列がリテラルとして保存されていること");
    }

    @Test
    @DisplayName("投稿日時の降順（最新順）でメッセージ一覧が取得できること")
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
                () -> assertEquals(3, page.getTotalSize(), "総件数は3件であること"),
                () -> assertEquals(3, posts.size(), "現在ページの件数は3件であること"),
                () -> assertEquals(third.id(), posts.get(0).id(), "先頭は最新投稿（third）であること"),
                () -> assertEquals(second.id(), posts.get(1).id(), "2番目は2番目に新しい投稿（second）であること"),
                () -> assertEquals(first.id(), posts.get(2).id(), "末尾は最古の投稿（first）であること")
        );
    }

    static Stream<Arguments> paginationTestCases() {
        return Stream.of(
                Arguments.of(0, 3, List.of("タイトル5", "タイトル4", "タイトル3")),
                Arguments.of(1, 2, List.of("タイトル2", "タイトル1")),
                Arguments.of(2, 0, List.of())
        );
    }

    @ParameterizedTest(name = "ページ番号: {0} でのページネーション検証（期待件数: {1}）")
    @MethodSource("paginationTestCases")
    @DisplayName("指定したページインデックスおよびサイズで正確に全件ページネーションされること")
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
                () -> assertEquals(5, page.getTotalSize(), "総件数は5件であること"),
                () -> assertEquals(2, page.getTotalPages(), "総ページ数は2ページであること"),
                () -> assertEquals(expectedSize, page.getContent().size(), "ページ内件数が一致すること"),
                () -> assertEquals(expectedTitles, actualTitles, "ページ内の全タイトルおよび順序が完全に一致すること")
        );
    }

    @Test
    @DisplayName("レコードが0件の場合、nullではなく空のコレクション [] を保証して返却すること（契約防御）")
    void testFindAllGuaranteesEmptyListWhenNoRecords() {
        Pageable pageable = Pageable.from(0, 50, Sort.of(Sort.Order.desc("createdAt")));
        Page<PostEntity> page = postRepository.findAll(pageable);

        assertNotNull(page, "ページ結果はnullであってはならない");
        assertNotNull(page.getContent(), "コンテンツリストは決してnullであってはならない");
        assertTrue(page.getContent().isEmpty(), "0件の場合は空のコレクション [] でなければならない");
        assertEquals(0, page.getTotalSize(), "総レコード数は0でなければならない");
    }

    @Test
    @DisplayName("事前条件違反: 必須項目（名前）がnullの投稿を保存しようとした場合、例外が発生すること")
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
                "NOT NULL制約違反によりDataAccessExceptionが発生すること");
    }
}
