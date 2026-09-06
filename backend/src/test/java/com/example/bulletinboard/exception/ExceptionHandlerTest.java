package com.example.bulletinboard.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RFC 9457 (Problem Details for HTTP APIs) 例外ハンドラーおよびエラー封筒DTOの統合・単体テスト。
 */
@MicronautTest(startApplication = false)
@DisplayName("RFC 9457 例外ハンドラーおよびエラーレスポンスのテスト")
class ExceptionHandlerTest {

    private static final String PROBLEM_JSON_MEDIA_TYPE = "application/problem+json";

    @Inject
    JsonMapper jsonMapper;

    @Test
    @DisplayName("入力検証エラー発生時: 400 Bad Request と RFC 9457 形式の Problem Details が返却されること")
    void handleConstraintViolationException() {
        ValidationExceptionHandler handler = new ValidationExceptionHandler();
        HttpRequest<?> request = HttpRequest.POST("/api/messages", Collections.emptyMap());

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("createMessage.request.name");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("名前は必須であり、空白のみでは指定できません");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("createMessage.request.email");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("メールアドレスの形式が正しくありません");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation1, violation2));

        HttpResponse<ProblemDetails> response = handler.handle(request, exception);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "ステータスコードは400であること"),
                () -> assertTrue(response.getContentType().isPresent(), "ContentTypeが存在すること"),
                () -> assertEquals(MediaType.of(PROBLEM_JSON_MEDIA_TYPE), response.getContentType().get(), "ContentTypeがapplication/problem+jsonであること"),
                () -> assertNotNull(response.body(), "応答ボディが存在すること")
        );

        ProblemDetails body = response.body();
        assertAll(
                () -> assertEquals("https://api.bulletin-board.local/errors/invalid-request", body.type()),
                () -> assertEquals("Bad Request", body.title()),
                () -> assertEquals(400, body.status()),
                () -> assertEquals("/api/messages", body.instance()),
                () -> assertTrue(body.detail().contains("入力値検証に失敗しました")),
                () -> assertNotNull(body.invalidParams(), "invalid_paramsが存在すること"),
                () -> assertEquals(2, body.invalidParams().size(), "2件のエラーが含まれること")
        );

        assertTrue(body.invalidParams().stream().anyMatch(p -> "name".equals(p.name()) && p.reason().contains("名前")));
        assertTrue(body.invalidParams().stream().anyMatch(p -> "email".equals(p.name()) && p.reason().contains("メールアドレス")));
    }

    @Test
    @DisplayName("システム内部例外発生時: 500 Internal Server Error とスタックトレースを隠蔽した Problem Details が返却されること")
    void handleGlobalUnexpectedException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpRequest<?> request = HttpRequest.GET("/api/messages");

        RuntimeException internalException = new RuntimeException("Sensitive database credentials or SQL syntax error");

        HttpResponse<ProblemDetails> response = handler.handle(request, internalException);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus(), "ステータスコードは500であること"),
                () -> assertTrue(response.getContentType().isPresent(), "ContentTypeが存在すること"),
                () -> assertEquals(MediaType.of(PROBLEM_JSON_MEDIA_TYPE), response.getContentType().get(), "ContentTypeがapplication/problem+jsonであること"),
                () -> assertNotNull(response.body(), "応答ボディが存在すること")
        );

        ProblemDetails body = response.body();
        assertAll(
                () -> assertEquals("https://api.bulletin-board.local/errors/internal-server-error", body.type()),
                () -> assertEquals("Internal Server Error", body.title()),
                () -> assertEquals(500, body.status()),
                () -> assertEquals("/api/messages", body.instance()),
                () -> assertEquals("内部サーバーエラーが発生しました。時間をおいて再試行してください。", body.detail(), "機密情報やスタックトレースが漏洩していないこと"),
                () -> assertNull(body.invalidParams(), "500エラー時はinvalid_paramsがnullであること")
        );
    }

    @Test
    @DisplayName("リクエストがnullの場合でも安全にフォールバックして処理できること")
    void handleWithNullRequest() {
        ValidationExceptionHandler validationHandler = new ValidationExceptionHandler();
        HttpResponse<ProblemDetails> valResponse = validationHandler.handle(null, new ConstraintViolationException(Collections.emptySet()));
        assertNotNull(valResponse.body());
        assertEquals("", valResponse.body().instance());

        GlobalExceptionHandler globalHandler = new GlobalExceptionHandler();
        HttpResponse<ProblemDetails> globResponse = globalHandler.handle(null, new RuntimeException("err"));
        assertNotNull(globResponse.body());
        assertEquals("", globResponse.body().instance());
    }

    @Test
    @DisplayName("不変条件保証: 不正な引数を渡した場合にNullPointerExceptionがスローされること")
    void constructorNullSafety() {
        assertThrows(NullPointerException.class, () -> new InvalidParam(null, "reason"));
        assertThrows(NullPointerException.class, () -> new InvalidParam("name", null));
        assertThrows(NullPointerException.class, () -> new ProblemDetails(null, "title", 400, "detail", "/", null));
        assertThrows(NullPointerException.class, () -> new ProblemDetails("type", null, 400, "detail", "/", null));
        assertThrows(NullPointerException.class, () -> new ProblemDetails("type", "title", 400, null, "/", null));
        assertThrows(NullPointerException.class, () -> new ProblemDetails("type", "title", 400, "detail", null, null));
    }

    @Test
    @DisplayName("RFC 9457 仕様検証: JSON シリアライズ時に 'invalid_params' のキー名および各属性が正しく出力されること")
    void serializeProblemDetailsWithInvalidParams() throws IOException {
        ProblemDetails details = new ProblemDetails(
                "https://api.bulletin-board.local/errors/invalid-request",
                "Bad Request",
                400,
                "入力値検証に失敗しました",
                "/api/messages",
                List.of(new InvalidParam("name", "名前は必須です"))
        );

        String json = jsonMapper.writeValueAsString(details);

        assertAll(
                () -> assertTrue(json.contains("\"type\":\"https://api.bulletin-board.local/errors/invalid-request\"")),
                () -> assertTrue(json.contains("\"title\":\"Bad Request\"")),
                () -> assertTrue(json.contains("\"status\":400")),
                () -> assertTrue(json.contains("\"detail\":\"入力値検証に失敗しました\"")),
                () -> assertTrue(json.contains("\"instance\":\"/api/messages\"")),
                () -> assertTrue(json.contains("\"invalid_params\":["), "キー名がsnake_caseのinvalid_paramsであること"),
                () -> assertTrue(json.contains("\"name\":\"name\"")),
                () -> assertTrue(json.contains("\"reason\":\"名前は必須です\""))
        );
    }

    @Test
    @DisplayName("RFC 9457 仕様検証: invalid_params が null の場合、JSON 出力からキー自体が除外されること")
    void serializeProblemDetailsWithoutInvalidParams() throws IOException {
        ProblemDetails details = new ProblemDetails(
                "https://api.bulletin-board.local/errors/internal-server-error",
                "Internal Server Error",
                500,
                "内部サーバーエラー",
                "/api/messages",
                null
        );

        String json = jsonMapper.writeValueAsString(details);

        assertAll(
                () -> assertTrue(json.contains("\"status\":500")),
                () -> assertFalse(json.contains("invalid_params"), "nullの場合はinvalid_paramsキーが含まれないこと")
        );
    }
}
