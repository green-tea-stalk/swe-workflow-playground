package com.example.bulletinboard.service;

import com.example.bulletinboard.dto.MessageCreateRequest;
import com.example.bulletinboard.dto.MessageResponse;
import com.example.bulletinboard.dto.PageResponse;
import com.example.bulletinboard.entity.MessageEntity;
import com.example.bulletinboard.exception.DatabaseAccessException;
import com.example.bulletinboard.repository.MessageRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false)
@DisplayName("掲示板メッセージドメインサービスの結合テスト")
class MessageServiceTest {

    @Inject
    MessageService messageService;

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
    @DisplayName("有効なリクエストからメッセージを作成し、前後の空白がトリムされて永続化されること")
    void createMessageWithTrimmedInputs() {
        Instant before = Instant.now();
        MessageCreateRequest request = new MessageCreateRequest(
                "  山田 太郎  ",
                "  yamada@example.com  ",
                "  タイトルテスト  ",
                "  本文メッセージです。  "
        );

        MessageResponse response = messageService.createMessage(request);

        assertAll(
                () -> assertNotNull(response.id(), "採番されたIDが存在すること"),
                () -> assertEquals("山田 太郎", response.name(), "名前がトリムされていること"),
                () -> assertEquals("yamada@example.com", response.email(), "メールアドレスがトリムされていること"),
                () -> assertEquals("タイトルテスト", response.title(), "タイトルがトリムされていること"),
                () -> assertEquals("本文メッセージです。", response.message(), "本文がトリムされていること"),
                () -> assertNotNull(response.createdAt(), "作成日時が付与されていること"),
                () -> assertTrue(
                        Duration.between(before, response.createdAt()).abs().toSeconds() <= 2,
                        "作成日時が現在時刻（UTC）と概ね一致すること"
                )
        );
    }

    @Test
    @DisplayName("空文字または空白のみのメールアドレスはnullとして正規化されて作成されること")
    void createMessageWithBlankEmailNormalizedToNull() {
        MessageCreateRequest request = new MessageCreateRequest(
                "名無しさん",
                "   ",
                "メールなし投稿",
                "メッセージ本文です。"
        );

        MessageResponse response = messageService.createMessage(request);

        assertAll(
                () -> assertNotNull(response.id()),
                () -> assertEquals("名無しさん", response.name()),
                () -> assertNull(response.email(), "空白のみのメールアドレスはnullに正規化されること"),
                () -> assertEquals("メールなし投稿", response.title()),
                () -> assertEquals("メッセージ本文です。", response.message())
        );
    }

    static Stream<Arguments> paginationTestCases() {
        return Stream.of(
                Arguments.of(0, 3, List.of("メッセージ5", "メッセージ4", "メッセージ3")),
                Arguments.of(1, 2, List.of("メッセージ2", "メッセージ1")),
                Arguments.of(2, 0, List.of())
        );
    }

    @ParameterizedTest(name = "ページ: {0}（期待件数: {1}）のページネーション取得検証")
    @MethodSource("paginationTestCases")
    @DisplayName("投稿日時の降順およびID降順タイブレークで正確にページネーションされた一覧が取得できること")
    void findMessagesWithPaginationDescending(int page, int expectedSize, List<String> expectedTitles) {
        for (int i = 1; i <= 5; i++) {
            messageService.createMessage(new MessageCreateRequest(
                    "投稿者" + i,
                    null,
                    "メッセージ" + i,
                    "本文" + i
            ));
        }

        PageResponse<MessageResponse> pageResponse = messageService.findMessages(page, 3);
        List<String> actualTitles = pageResponse.content().stream().map(MessageResponse::title).toList();

        assertAll(
                () -> assertEquals(5, pageResponse.totalElements(), "総件数は5件であること"),
                () -> assertEquals(2, pageResponse.totalPages(), "総ページ数は2ページであること"),
                () -> assertEquals(page, pageResponse.page(), "現在ページインデックスが一致すること"),
                () -> assertEquals(3, pageResponse.size(), "要求ページサイズが一致すること"),
                () -> assertEquals(expectedSize, pageResponse.content().size(), "ページ内件数が一致すること"),
                () -> assertEquals(expectedTitles, actualTitles, "タイトルの降順ソート順序が一致すること")
        );
    }

    @Test
    @DisplayName("事前条件違反: 負のページ番号を指定した場合にIllegalArgumentExceptionがスローされること")
    void findMessagesWithNegativePageThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.findMessages(-1, 10)
        );
        assertTrue(exception.getMessage().contains("pageは0以上でなければなりません"));
    }

    @Test
    @DisplayName("事前条件違反: 1未満のページサイズを指定した場合にIllegalArgumentExceptionがスローされること")
    void findMessagesWithInvalidSizeThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.findMessages(0, 0)
        );
        assertTrue(exception.getMessage().contains("sizeは1以上"));
    }

    @Test
    @DisplayName("事前条件違反: 最大許容ページサイズ（100件）を超過した場合にIllegalArgumentExceptionがスローされること")
    void findMessagesWithSizeExceedingMaxThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.findMessages(0, MessageService.MAX_PAGE_SIZE + 1)
        );
        assertTrue(exception.getMessage().contains("sizeは1以上100以下でなければなりません"));
    }

    @Test
    @DisplayName("事前条件違反: nullのリクエストを指定した場合に例外がスローされること")
    void createMessageWithNullRequestThrowsException() {
        assertThrows(
                Exception.class,
                () -> messageService.createMessage(null)
        );
    }

    static Stream<Arguments> invalidMessageCreateRequests() {
        return Stream.of(
                // 名前バリデーション
                Arguments.of(null, "user@example.com", "タイトル", "本文", "名前がnull"),
                Arguments.of("", "user@example.com", "タイトル", "本文", "名前が空文字"),
                Arguments.of("   ", "user@example.com", "タイトル", "本文", "名前が空白文字のみ"),
                Arguments.of("a".repeat(51), "user@example.com", "タイトル", "本文", "名前が51文字（上限超過）"),

                // メールアドレスバリデーション
                Arguments.of("名前", "not-an-email", "タイトル", "本文", "メールアドレス形式不正"),
                Arguments.of("名前", "@missing-user.com", "タイトル", "本文", "メールアドレスのローカル部欠落"),
                Arguments.of("名前", "user@.com", "タイトル", "本文", "メールアドレスのドメイン形式不正"),
                Arguments.of("名前", "a".repeat(90) + "@example.com", "タイトル", "本文", "メールアドレスが102文字（上限超過）"),

                // タイトルバリデーション
                Arguments.of("名前", "user@example.com", null, "本文", "タイトルがnull"),
                Arguments.of("名前", "user@example.com", "", "本文", "タイトルが空文字"),
                Arguments.of("名前", "user@example.com", "   ", "本文", "タイトルが空白文字のみ"),
                Arguments.of("名前", "user@example.com", "a".repeat(101), "本文", "タイトルが101文字（上限超過）"),

                // 本文バリデーション
                Arguments.of("名前", "user@example.com", "タイトル", null, "本文がnull"),
                Arguments.of("名前", "user@example.com", "タイトル", "", "本文が空文字"),
                Arguments.of("名前", "user@example.com", "タイトル", "   ", "本文が空白文字のみ"),
                Arguments.of("名前", "user@example.com", "タイトル", "a".repeat(1001), "本文が1001文字（上限超過）")
        );
    }

    @ParameterizedTest(name = "不正リクエスト検証: {4}")
    @MethodSource("invalidMessageCreateRequests")
    @DisplayName("事前条件違反: 入力制約を満たさないリクエストの場合にConstraintViolationExceptionがスローされること")
    void createMessageWithInvalidRequestThrowsConstraintViolationException(
            String name, String email, String title, String message, String description
    ) {
        MessageCreateRequest request = new MessageCreateRequest(name, email, title, message);

        assertThrows(
                ConstraintViolationException.class,
                () -> messageService.createMessage(request),
                description + " の場合にバリデーション例外がスローされること"
        );
    }

    @Test
    @DisplayName("事後条件保証: データベース書き込み失敗時にトランザクションがロールバックされDatabaseAccessExceptionが伝播すること")
    void createMessageWhenDatabaseFailsThrowsDatabaseAccessException() {
        MessageRepository failingRepo = mock(MessageRepository.class);
        when(failingRepo.save(any())).thenThrow(new RuntimeException("JDBC connection failure"));

        MessageService serviceWithFailure = new MessageService(failingRepo);
        MessageCreateRequest request = new MessageCreateRequest(
                "山田 太郎",
                "yamada@example.com",
                "タイトル",
                "本文"
        );

        DatabaseAccessException exception = assertThrows(
                DatabaseAccessException.class,
                () -> serviceWithFailure.createMessage(request)
        );

        assertTrue(exception.getMessage().contains("データベースへのメッセージ保存中にエラーが発生しました"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("事後条件保証: データベース読み取り失敗時にDatabaseAccessExceptionが伝播すること")
    void findMessagesWhenDatabaseFailsThrowsDatabaseAccessException() {
        MessageRepository failingRepo = mock(MessageRepository.class);
        when(failingRepo.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Query timeout"));

        MessageService serviceWithFailure = new MessageService(failingRepo);

        DatabaseAccessException exception = assertThrows(
                DatabaseAccessException.class,
                () -> serviceWithFailure.findMessages(0, 10)
        );

        assertTrue(exception.getMessage().contains("データベースからのメッセージ取得中にエラーが発生しました"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("不変条件保証: DTOファクトリメソッドにnullを渡した場合にNullPointerExceptionがスローされること")
    void dtoFactoryMethodsWithNullThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> MessageResponse.fromEntity(null));
        assertThrows(NullPointerException.class, () -> PageResponse.fromPage(null, MessageResponse::fromEntity));
        assertThrows(NullPointerException.class, () -> PageResponse.fromPage(mock(Page.class), null));
    }
}
