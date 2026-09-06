package com.example.bulletinboard.controller;

import com.example.bulletinboard.dto.MessageCreateRequest;
import com.example.bulletinboard.dto.MessageResponse;
import com.example.bulletinboard.dto.PageResponse;
import com.example.bulletinboard.exception.ProblemDetails;
import com.example.bulletinboard.repository.MessageRepository;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
@DisplayName("掲示板メッセージコントローラーの結合テスト")
class MessageControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

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
    @DisplayName("正常系: 有効なメッセージ投稿リクエストで201 Createdと採番されたメッセージが返却されること")
    void createMessageSuccessfully() {
        MessageCreateRequest request = new MessageCreateRequest(
                "  山田 太郎  ",
                "  yamada@example.com  ",
                "  初めての投稿  ",
                "  こんにちは、Micronaut掲示板です。  "
        );

        HttpRequest<MessageCreateRequest> httpRequest = HttpRequest.POST("/api/messages", request);
        HttpResponse<MessageResponse> response = client.toBlocking().exchange(httpRequest, MessageResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.CREATED, response.getStatus(), "ステータスコードは201 Createdであること"),
                () -> assertNotNull(response.body(), "レスポンスボディが存在すること"),
                () -> assertNotNull(response.body().id(), "採番されたIDが存在すること"),
                () -> assertEquals("山田 太郎", response.body().name(), "名前がトリムされていること"),
                () -> assertEquals("yamada@example.com", response.body().email(), "メールアドレスがトリムされていること"),
                () -> assertEquals("初めての投稿", response.body().title(), "タイトルがトリムされていること"),
                () -> assertEquals("こんにちは、Micronaut掲示板です。", response.body().message(), "本文がトリムされていること"),
                () -> assertNotNull(response.body().createdAt(), "作成日時が付与されていること")
        );
    }

    @Test
    @DisplayName("正常系: メールアドレス未指定または空白の場合、201 Createdでemailがnullとして登録されること")
    void createMessageWithBlankEmailReturnsNullEmail() {
        MessageCreateRequest request = new MessageCreateRequest(
                "匿名希望",
                "   ",
                "メールなし投稿",
                "本文です。"
        );

        HttpRequest<MessageCreateRequest> httpRequest = HttpRequest.POST("/api/messages", request);
        HttpResponse<MessageResponse> response = client.toBlocking().exchange(httpRequest, MessageResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.CREATED, response.getStatus()),
                () -> assertNotNull(response.body()),
                () -> assertNull(response.body().email(), "空白メールはnullに正規化されること")
        );
    }

    @Test
    @DisplayName("正常系: メッセージ一覧取得（デフォルトページネーション: page=0, size=50）で200 OKと一覧が返却されること")
    void getMessagesWithDefaultPagination() {
        for (int i = 1; i <= 3; i++) {
            client.toBlocking().exchange(
                    HttpRequest.POST("/api/messages", new MessageCreateRequest("投稿者" + i, null, "タイトル" + i, "本文" + i)),
                    MessageResponse.class
            );
        }

        HttpRequest<?> httpRequest = HttpRequest.GET("/api/messages");
        @SuppressWarnings("unchecked")
        Argument<PageResponse<MessageResponse>> pageArgument = (Argument<PageResponse<MessageResponse>>) (Argument<?>) Argument.of(PageResponse.class, MessageResponse.class);
        HttpResponse<PageResponse<MessageResponse>> response = client.toBlocking().exchange(httpRequest, pageArgument);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatus(), "ステータスコードは200 OKであること"),
                () -> assertNotNull(response.body()),
                () -> assertEquals(3, response.body().totalElements()),
                () -> assertEquals(1, response.body().totalPages()),
                () -> assertEquals(0, response.body().page()),
                () -> assertEquals(50, response.body().size()),
                () -> assertEquals(3, response.body().content().size())
        );

        List<String> titles = response.body().content().stream().map(MessageResponse::title).toList();
        assertEquals(List.of("タイトル3", "タイトル2", "タイトル1"), titles, "作成日時の降順で返却されること");
    }

    @Test
    @DisplayName("正常系: クエリパラメータ指定（page=0, size=2）で正しくページネーションされること")
    void getMessagesWithCustomPagination() {
        for (int i = 1; i <= 3; i++) {
            client.toBlocking().exchange(
                    HttpRequest.POST("/api/messages", new MessageCreateRequest("投稿者" + i, null, "タイトル" + i, "本文" + i)),
                    MessageResponse.class
            );
        }

        HttpRequest<?> httpRequest = HttpRequest.GET("/api/messages?page=0&size=2");
        @SuppressWarnings("unchecked")
        Argument<PageResponse<MessageResponse>> pageArgument = (Argument<PageResponse<MessageResponse>>) (Argument<?>) Argument.of(PageResponse.class, MessageResponse.class);
        HttpResponse<PageResponse<MessageResponse>> response = client.toBlocking().exchange(httpRequest, pageArgument);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatus()),
                () -> assertNotNull(response.body()),
                () -> assertEquals(3, response.body().totalElements()),
                () -> assertEquals(2, response.body().totalPages()),
                () -> assertEquals(0, response.body().page()),
                () -> assertEquals(2, response.body().size()),
                () -> assertEquals(2, response.body().content().size()),
                () -> assertEquals("タイトル3", response.body().content().get(0).title()),
                () -> assertEquals("タイトル2", response.body().content().get(1).title())
        );
    }

    static Stream<Arguments> invalidPostRequests() {
        return Stream.of(
                Arguments.of(new MessageCreateRequest("", "user@example.com", "タイトル", "本文"), "名前が空文字"),
                Arguments.of(new MessageCreateRequest("   ", "user@example.com", "タイトル", "本文"), "名前が空白文字のみ"),
                Arguments.of(new MessageCreateRequest("a".repeat(51), "user@example.com", "タイトル", "本文"), "名前が51文字（上限超過）"),
                Arguments.of(new MessageCreateRequest("名前", "not-an-email", "タイトル", "本文"), "メールアドレス形式不正"),
                Arguments.of(new MessageCreateRequest("名前", "user@example.com", "", "本文"), "タイトルが空文字"),
                Arguments.of(new MessageCreateRequest("名前", "user@example.com", "   ", "本文"), "タイトルが空白文字のみ"),
                Arguments.of(new MessageCreateRequest("名前", "user@example.com", "タイトル", ""), "本文が空文字"),
                Arguments.of(new MessageCreateRequest("名前", "user@example.com", "タイトル", "   "), "本文が空白文字のみ")
        );
    }

    @ParameterizedTest(name = "バリデーション違反検証: {1}")
    @MethodSource("invalidPostRequests")
    @DisplayName("異常系: 不正なメッセージ投稿リクエストで400 Bad RequestとProblem Detailsが返却されること")
    void createMessageWithInvalidInputReturnsBadRequest(MessageCreateRequest invalidRequest, String description) {
        HttpRequest<MessageCreateRequest> httpRequest = HttpRequest.POST("/api/messages", invalidRequest);

        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(httpRequest, Argument.of(MessageResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus(), description + " の場合に400 Bad Requestが返ること");
        assertTrue(exception.getResponse().getBody(ProblemDetails.class).isPresent(), "ProblemDetailsボディが存在すること");

        ProblemDetails details = exception.getResponse().getBody(ProblemDetails.class).get();
        assertAll(
                () -> assertEquals("https://api.bulletin-board.local/errors/invalid-request", details.type()),
                () -> assertEquals("Bad Request", details.title()),
                () -> assertEquals(400, details.status()),
                () -> assertEquals("/api/messages", details.instance()),
                () -> assertNotNull(details.invalidParams(), "invalid_paramsが存在すること"),
                () -> assertFalse(details.invalidParams().isEmpty(), "1件以上のエラー詳細が含まれること")
        );
    }

    static Stream<Arguments> invalidQueryParams() {
        return Stream.of(
                Arguments.of("page=-1&size=50", "負のページ番号"),
                Arguments.of("page=0&size=0", "1未満のサイズ"),
                Arguments.of("page=0&size=101", "100超過のサイズ")
        );
    }

    @ParameterizedTest(name = "不正クエリパラメータ検証: {1}")
    @MethodSource("invalidQueryParams")
    @DisplayName("異常系: 不正なクエリパラメータ指定で400 Bad Requestが返却されること")
    void getMessagesWithInvalidQueryParamsReturnsBadRequest(String query, String description) {
        HttpRequest<?> httpRequest = HttpRequest.GET("/api/messages?" + query);

        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(httpRequest, Argument.of(PageResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus(), description + " の場合に400 Bad Requestが返ること");
        assertTrue(exception.getResponse().getBody(ProblemDetails.class).isPresent());
    }

    @Test
    @DisplayName("CORS検証: 許可されたオリジンからのリクエストでAccess-Control-Allow-Originヘッダーが返却されること")
    void corsHeadersVerification() {
        HttpRequest<?> httpRequest = HttpRequest.GET("/api/messages")
                .header("Origin", "http://localhost:4200");

        HttpResponse<?> response = client.toBlocking().exchange(httpRequest);

        assertEquals("http://localhost:4200", response.header("Access-Control-Allow-Origin"), "許可されたオリジンがCORSヘッダーに含まれること");
    }

    @Test
    @DisplayName("CORS検証: 許可されていない悪意あるオリジンからのリクエストが403 Forbiddenで拒絶されること")
    void corsDisallowedOriginVerification() {
        HttpRequest<?> httpRequest = HttpRequest.GET("/api/messages")
                .header("Origin", "http://unauthorized.evil.com");

        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(httpRequest)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus(), "未許可オリジンからのリクエストは403 Forbiddenで拒絶されること");
    }

    @Test
    @DisplayName("CORS検証: OPTIONSプリフライトリクエストで許可メソッドおよびオリジンが正しく返却されること")
    void corsPreflightOptionsVerification() {
        HttpRequest<?> httpRequest = HttpRequest.OPTIONS("/api/messages")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type");

        HttpResponse<?> response = client.toBlocking().exchange(httpRequest);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatus(), "プリフライトは200 OKで応答すること"),
                () -> assertEquals("http://localhost:4200", response.header("Access-Control-Allow-Origin")),
                () -> assertTrue(response.header("Access-Control-Allow-Methods").contains("POST"))
        );
    }

    @Test
    @DisplayName("不変条件保証: MessageControllerコンストラクタにnullを渡した場合にNullPointerExceptionがスローされること")
    void constructorNullSafety() {
        assertThrows(NullPointerException.class, () -> new MessageController(null));
    }
}
