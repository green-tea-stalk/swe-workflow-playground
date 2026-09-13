package com.example.bulletinboard.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import com.example.bulletinboard.service.DefaultMessageLocalizationService;
import com.example.bulletinboard.service.MessageLocalizationService;
import com.example.bulletinboard.util.HttpLocaleResolver;
import com.example.bulletinboard.util.LocaleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for custom exception handlers verifying RFC 9457 Problem Details envelopes.
 */
@DisplayName("Unit test suite for RFC 9457 exception handlers")
class ExceptionHandlerTest {

    private LocaleResolver localeResolver;
    private MessageLocalizationService messageLocalizationService;

    @BeforeEach
    void setUp() {
        localeResolver = new HttpLocaleResolver();
        messageLocalizationService = new DefaultMessageLocalizationService();
    }

    @ParameterizedTest(name = "GlobalExceptionHandler with Accept-Language: {0} resolves title: {1}")
    @CsvSource({
            "'ja', 'サーバー内部エラー', 'リクエストの処理中に予期せぬエラーが発生しました。', 'ja'",
            "'en', 'Internal Server Error', 'An unexpected error occurred while processing the request.', 'en'",
            "'', 'Internal Server Error', 'An unexpected error occurred while processing the request.', 'en'"
    })
    @DisplayName("GlobalExceptionHandler: should return localized 500 ProblemDetails and Content-Language header")
    void testGlobalExceptionHandlerLocalized(String acceptLanguage, String expectedTitle, String expectedDetail, String expectedLang) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = acceptLanguage.isBlank()
                ? HttpRequest.GET("/api/posts")
                : HttpRequest.GET("/api/posts").header("Accept-Language", acceptLanguage);
        RuntimeException cause = new RuntimeException("Database connection dropped unexpectedly");

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));
        assertEquals(expectedLang, response.getHeaders().get("Content-Language"));

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("https://example.com/errors/internal-server-error", problem.type());
        assertEquals(expectedTitle, problem.title());
        assertEquals(500, problem.status());
        assertEquals(expectedDetail, problem.detail());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @Test
    @DisplayName("GlobalExceptionHandler: should enforce non-null preconditions on constructor and handle method")
    void testGlobalExceptionHandlerPreconditions() {
        assertThrows(NullPointerException.class, () -> new GlobalExceptionHandler(null, messageLocalizationService));
        assertThrows(NullPointerException.class, () -> new GlobalExceptionHandler(localeResolver, null));

        GlobalExceptionHandler handler = new GlobalExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.GET("/api/posts");
        RuntimeException cause = new RuntimeException("Error");

        assertThrows(NullPointerException.class, () -> handler.handle(null, cause));
        assertThrows(NullPointerException.class, () -> handler.handle(request, null));
    }

    @ParameterizedTest(name = "IllegalArgumentExceptionHandler with Accept-Language: {0} resolves title: {1}")
    @CsvSource({
            "'ja', '不正な引数', 'ja'",
            "'en', 'Invalid Argument', 'en'",
            "'', 'Invalid Argument', 'en'"
    })
    @DisplayName("IllegalArgumentExceptionHandler: should return localized 400 ProblemDetails and Content-Language header")
    void testIllegalArgumentExceptionHandlerLocalized(String acceptLanguage, String expectedTitle, String expectedLang) {
        IllegalArgumentExceptionHandler handler = new IllegalArgumentExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = acceptLanguage.isBlank()
                ? HttpRequest.POST("/api/posts", "{}")
                : HttpRequest.POST("/api/posts", "{}").header("Accept-Language", acceptLanguage);
        IllegalArgumentException cause = new IllegalArgumentException("Pagination size must be between 1 and 50");

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));
        assertEquals(expectedLang, response.getHeaders().get("Content-Language"));

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("https://example.com/errors/invalid-argument", problem.type());
        assertEquals(expectedTitle, problem.title());
        assertEquals(400, problem.status());
        assertEquals("Pagination size must be between 1 and 50", problem.detail());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @ParameterizedTest(name = "Sanitizing dangerous or empty message: ''{0}''")
    @CsvSource({
            "'', 'Invalid Argument'",
            "'   ', 'Invalid Argument'",
            "'com.example.bulletinboard.InternalClass failure', 'Invalid Argument'",
            "'Unexpected NullPointerException occurred', 'Invalid Argument'"
    })
    @DisplayName("IllegalArgumentExceptionHandler: should sanitize internal exceptions or empty message to title")
    void testIllegalArgumentExceptionHandlerSanitization(String rawMessage, String expectedDetail) {
        IllegalArgumentExceptionHandler handler = new IllegalArgumentExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");
        IllegalArgumentException cause = new IllegalArgumentException(rawMessage);

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);
        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals(expectedDetail, problem.detail());
    }

    @Test
    @DisplayName("IllegalArgumentExceptionHandler: should handle null exception message safely")
    void testIllegalArgumentExceptionHandlerNullMessage() {
        IllegalArgumentExceptionHandler handler = new IllegalArgumentExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");
        IllegalArgumentException cause = new IllegalArgumentException((String) null);

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);
        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("Invalid Argument", problem.detail());
    }

    @Test
    @DisplayName("IllegalArgumentExceptionHandler: should enforce non-null preconditions on constructor and handle method")
    void testIllegalArgumentExceptionHandlerPreconditions() {
        assertThrows(NullPointerException.class, () -> new IllegalArgumentExceptionHandler(null, messageLocalizationService));
        assertThrows(NullPointerException.class, () -> new IllegalArgumentExceptionHandler(localeResolver, null));

        IllegalArgumentExceptionHandler handler = new IllegalArgumentExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");
        IllegalArgumentException cause = new IllegalArgumentException("Invalid");

        assertThrows(NullPointerException.class, () -> handler.handle(null, cause));
        assertThrows(NullPointerException.class, () -> handler.handle(request, null));
    }

    @ParameterizedTest(name = "ValidationExceptionHandler with Accept-Language: {0} resolves title: {1}, reason: {2}")
    @CsvSource({
            "'ja', '入力値検証エラー', '入力内容に不備があります。制約条件を確認してください。', '{validation.name.required}', '名前を入力してください', 'ja'",
            "'en', 'Validation Failed', 'Input payload failed validation constraints.', '{validation.name.required}', 'Name must not be blank', 'en'",
            "'', 'Validation Failed', 'Input payload failed validation constraints.', '{validation.name.required}', 'Name must not be blank', 'en'"
    })
    @DisplayName("ValidationExceptionHandler: should return localized 400 ProblemDetails and Content-Language header")
    @SuppressWarnings("unchecked")
    void testValidationExceptionHandlerLocalized(String acceptLanguage, String expectedTitle, String expectedDetail,
                                                String messageTemplate, String expectedReason, String expectedLang) {
        ValidationExceptionHandler handler = new ValidationExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = acceptLanguage.isBlank()
                ? HttpRequest.POST("/api/posts", "{}")
                : HttpRequest.POST("/api/posts", "{}").header("Accept-Language", acceptLanguage);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Path.Node node = mock(Path.Node.class);

        when(node.getName()).thenReturn("name");
        when(path.iterator()).thenReturn(List.of(node).iterator());
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessageTemplate()).thenReturn(messageTemplate);
        when(violation.getMessage()).thenReturn("Default unlocalized message");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        HttpResponse<ProblemDetails> response = handler.handle(request, ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));
        assertEquals(expectedLang, response.getHeaders().get("Content-Language"));

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals(expectedTitle, problem.title());
        assertEquals(expectedDetail, problem.detail());
        assertEquals(1, problem.invalidParams().size());
        assertEquals("name", problem.invalidParams().get(0).name());
        assertEquals(expectedReason, problem.invalidParams().get(0).reason());
    }

    @Test
    @DisplayName("ValidationExceptionHandler: should return guaranteed empty array when violation set is empty or null")
    void testValidationExceptionHandlerEmptyViolations() {
        ValidationExceptionHandler handler = new ValidationExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");

        ConstraintViolationException emptyEx = new ConstraintViolationException(Collections.emptySet());
        HttpResponse<ProblemDetails> emptyResponse = handler.handle(request, emptyEx);

        assertEquals(HttpStatus.BAD_REQUEST, emptyResponse.getStatus());
        ProblemDetails emptyProblem = emptyResponse.body();
        assertNotNull(emptyProblem);
        assertNotNull(emptyProblem.invalidParams());
        assertTrue(emptyProblem.invalidParams().isEmpty());

        ConstraintViolationException nullEx = new ConstraintViolationException(null);
        HttpResponse<ProblemDetails> nullResponse = handler.handle(request, nullEx);

        assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatus());
        ProblemDetails nullProblem = nullResponse.body();
        assertNotNull(nullProblem);
        assertNotNull(nullProblem.invalidParams());
        assertTrue(nullProblem.invalidParams().isEmpty());
    }

    @Test
    @DisplayName("ValidationExceptionHandler: should deduplicate multiple identical violations and filter null violations")
    @SuppressWarnings("unchecked")
    void testValidationExceptionHandlerDeduplicationAndNullFilter() {
        ValidationExceptionHandler handler = new ValidationExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Path.Node node = mock(Path.Node.class);

        when(node.getName()).thenReturn("name");
        when(path.iterator()).thenAnswer(inv -> List.of(node).iterator());
        when(violation1.getPropertyPath()).thenReturn(path);
        when(violation1.getMessageTemplate()).thenReturn("{validation.name.required}");
        when(violation1.getMessage()).thenReturn("Name must not be blank");

        when(violation2.getPropertyPath()).thenReturn(path);
        when(violation2.getMessageTemplate()).thenReturn("{validation.name.required}");
        when(violation2.getMessage()).thenReturn("Name must not be blank");

        // Pass set with 2 identical violations
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation1, violation2));
        HttpResponse<ProblemDetails> response = handler.handle(request, ex);

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals(1, problem.invalidParams().size(), "Duplicate violations must be deduplicated");
        assertEquals("name", problem.invalidParams().get(0).name());
        assertEquals("Name must not be blank", problem.invalidParams().get(0).reason());
    }

    @Test
    @DisplayName("ValidationExceptionHandler: should enforce non-null preconditions on constructor and handle method")
    void testValidationExceptionHandlerPreconditions() {
        assertThrows(NullPointerException.class, () -> new ValidationExceptionHandler(null, messageLocalizationService));
        assertThrows(NullPointerException.class, () -> new ValidationExceptionHandler(localeResolver, null));

        ValidationExceptionHandler handler = new ValidationExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());

        assertThrows(NullPointerException.class, () -> handler.handle(null, ex));
        assertThrows(NullPointerException.class, () -> handler.handle(request, null));
    }

    @Test
    @DisplayName("ValidationExceptionHandler: should fallback to path string representation when property path node name is blank")
    @SuppressWarnings("unchecked")
    void testValidationExceptionHandlerFallbackPropertyPath() {
        ValidationExceptionHandler handler = new ValidationExceptionHandler(localeResolver, messageLocalizationService);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Path.Node blankNode = mock(Path.Node.class);

        when(blankNode.getName()).thenReturn("");
        when(path.iterator()).thenReturn(List.of(blankNode).iterator());
        when(path.toString()).thenReturn("request.fallbackField");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessageTemplate()).thenReturn("Unresolvable raw message");
        when(violation.getMessage()).thenReturn("Must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        HttpResponse<ProblemDetails> response = handler.handle(request, ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals(1, problem.invalidParams().size());
        assertEquals("request.fallbackField", problem.invalidParams().get(0).name());
        assertEquals("Must not be null", problem.invalidParams().get(0).reason());
    }

    @Test
    @DisplayName("Domain exception hierarchy: PostValidationException and PostStorageException should be instantiated correctly")
    void testDomainExceptionHierarchy() {
        PostValidationException validationEx = new PostValidationException("Validation failed");
        assertEquals("Validation failed", validationEx.getMessage());
        assertNull(validationEx.getCause());

        Throwable rootCause = new IllegalStateException("Connection pool exhausted");
        PostStorageException storageEx = new PostStorageException("Storage failed", rootCause);
        assertEquals("Storage failed", storageEx.getMessage());
        assertSame(rootCause, storageEx.getCause());
    }
}
