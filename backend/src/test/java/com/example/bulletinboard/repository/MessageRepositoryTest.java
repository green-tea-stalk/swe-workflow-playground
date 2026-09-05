package com.example.bulletinboard.repository;

import com.example.bulletinboard.entity.MessageEntity;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
@DisplayName("掲示板メッセージリポジトリの統合テスト")
class MessageRepositoryTest {

    @Inject
    MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
    }

    @Test
    @DisplayName("メッセージを保存すると主キーIDが自動採番され永続化および再取得できること")
    void saveMessageSuccessfully() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        MessageEntity unsaved = new MessageEntity(
                null,
                "山田 太郎",
                "yamada@example.com",
                "初回投稿",
                "メッセージ本文です。",
                now
        );

        MessageEntity saved = messageRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id(), "保存後のIDはnullでないこと"),
                () -> assertTrue(saved.id() > 0, "保存後のIDは正の数であること"),
                () -> assertEquals("山田 太郎", saved.name(), "名前が一致すること"),
                () -> assertEquals("yamada@example.com", saved.email(), "メールアドレスが一致すること"),
                () -> assertEquals("初回投稿", saved.title(), "タイトルが一致すること"),
                () -> assertEquals("メッセージ本文です。", saved.message(), "メッセージ本文が一致すること"),
                () -> assertEquals(now, saved.createdAt().truncatedTo(ChronoUnit.SECONDS), "作成日時が一致すること")
        );

        Optional<MessageEntity> retrieved = messageRepository.findById(saved.id());
        assertTrue(retrieved.isPresent(), "データベースからIDで再取得できること");
        assertEquals(saved, retrieved.get(), "再取得したエンティティは保存後エンティティと一致すること");
    }

    @Test
    @DisplayName("メールアドレスがnullのメッセージも正常に保存および取得できること")
    void saveMessageWithNullEmailSuccessfully() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        MessageEntity unsaved = new MessageEntity(
                null,
                "名無しさん",
                null,
                "匿名タイトル",
                "メールなしの投稿",
                now
        );

        MessageEntity saved = messageRepository.save(unsaved);

        assertAll(
                () -> assertNotNull(saved.id()),
                () -> assertEquals("名無しさん", saved.name()),
                () -> assertNull(saved.email(), "メールアドレスはnullであること"),
                () -> assertEquals("匿名タイトル", saved.title()),
                () -> assertEquals("メールなしの投稿", saved.message())
        );
    }

    @Test
    @DisplayName("SQLインジェクション構文を含む文字列がリテラルとして安全に保存および取得できること")
    void saveMessageWithSqlInjectionPayloadLiteral() {
        String sqlInjectionTitle = "'); DROP TABLE messages; --";
        String sqlInjectionMessage = "' OR '1'='1";
        MessageEntity unsaved = new MessageEntity(
                null,
                "攻撃者テスト",
                "attacker@example.com",
                sqlInjectionTitle,
                sqlInjectionMessage,
                Instant.now().truncatedTo(ChronoUnit.SECONDS)
        );

        MessageEntity saved = messageRepository.save(unsaved);
        Optional<MessageEntity> retrieved = messageRepository.findById(saved.id());

        assertTrue(retrieved.isPresent(), "レコードが正常に取得できること");
        assertEquals(sqlInjectionTitle, retrieved.get().title(), "SQLインジェクション構文がエスケープされリテラルとして保存されていること");
        assertEquals(sqlInjectionMessage, retrieved.get().message(), "SQLインジェクション構文がエスケープされリテラルとして保存されていること");
    }

    @Test
    @DisplayName("投稿日時の降順（最新順）でメッセージ一覧が取得できること")
    void findMessagesOrderedByCreatedAtDescending() {
        Instant baseTime = Instant.parse("2026-09-05T10:00:00Z");

        MessageEntity first = messageRepository.save(new MessageEntity(
                null, "投稿者1", null, "タイトル1", "本文1", baseTime
        ));
        MessageEntity second = messageRepository.save(new MessageEntity(
                null, "投稿者2", null, "タイトル2", "本文2", baseTime.plus(1, ChronoUnit.HOURS)
        ));
        MessageEntity third = messageRepository.save(new MessageEntity(
                null, "投稿者3", null, "タイトル3", "本文3", baseTime.plus(2, ChronoUnit.HOURS)
        ));

        Pageable pageable = Pageable.from(0, 50, Sort.of(Sort.Order.desc("createdAt")));
        Page<MessageEntity> page = messageRepository.findAll(pageable);
        List<MessageEntity> messages = page.getContent();

        assertAll(
                () -> assertEquals(3, page.getTotalSize(), "総件数は3件であること"),
                () -> assertEquals(3, messages.size(), "現在ページの件数は3件であること"),
                () -> assertEquals(third.id(), messages.get(0).id(), "先頭は最新投稿（third）であること"),
                () -> assertEquals(second.id(), messages.get(1).id(), "2番目は2番目に新しい投稿（second）であること"),
                () -> assertEquals(first.id(), messages.get(2).id(), "末尾は最古の投稿（first）であること")
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
    void paginateMessagesCorrectly(int pageIndex, int expectedSize, List<String> expectedTitles) {
        Instant baseTime = Instant.parse("2026-09-05T10:00:00Z");
        for (int i = 1; i <= 5; i++) {
            messageRepository.save(new MessageEntity(
                    null, "投稿者" + i, null, "タイトル" + i, "本文" + i, baseTime.plus(i, ChronoUnit.MINUTES)
            ));
        }

        int pageSize = 3;
        Pageable pageable = Pageable.from(pageIndex, pageSize, Sort.of(Sort.Order.desc("createdAt")));
        Page<MessageEntity> page = messageRepository.findAll(pageable);
        List<String> actualTitles = page.getContent().stream().map(MessageEntity::title).toList();

        assertAll(
                () -> assertEquals(5, page.getTotalSize(), "総件数は5件であること"),
                () -> assertEquals(2, page.getTotalPages(), "総ページ数は2ページであること"),
                () -> assertEquals(expectedSize, page.getContent().size(), "ページ内件数が一致すること"),
                () -> assertEquals(expectedTitles, actualTitles, "ページ内の全タイトルおよび順序が完全に一致すること")
        );
    }

    @Test
    @DisplayName("事前条件違反: 必須項目（名前）がnullのメッセージを保存しようとした場合、例外が発生すること")
    void saveMessageWithNullNameThrowsException() {
        MessageEntity invalid = new MessageEntity(
                null,
                null,
                "test@example.com",
                "タイトル",
                "メッセージ本文",
                Instant.now()
        );

        assertThrows(Exception.class, () -> messageRepository.save(invalid),
                "NOT NULL制約違反により例外が発生すること");
    }
}
