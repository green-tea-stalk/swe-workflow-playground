package com.example.bulletinboard.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for custom exception handlers verifying RFC 9457 Problem Details envelopes.
 */
@DisplayName("RFC 9457 例外ハンドラーの単体テスト")
class ExceptionHandlerTest {

    @Test
    @DisplayName("GlobalExceptionHandler: 予期せぬ例外を捕捉し、スタックトレースを漏洩させずに500 ProblemDetailsを返却すること")
    void testGlobalExceptionHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpRequest<?> request = HttpRequest.GET("/api/posts");
        RuntimeException cause = new RuntimeException("Database connection dropped unexpectedly");

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("https://example.com/errors/internal-server-error", problem.type());
        assertEquals("Internal Server Error", problem.title());
        assertEquals(500, problem.status());
        assertEquals("An unexpected error occurred while processing the request.", problem.detail());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @Test
    @DisplayName("IllegalArgumentExceptionHandler: 不正引数例外を捕捉し、400 ProblemDetailsを返却すること")
    void testIllegalArgumentExceptionHandler() {
        IllegalArgumentExceptionHandler handler = new IllegalArgumentExceptionHandler();
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");
        IllegalArgumentException cause = new IllegalArgumentException("Pagination size must be between 1 and 50");

        HttpResponse<ProblemDetails> response = handler.handle(request, cause);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_PROBLEM_TYPE, response.getContentType().orElse(null));

        ProblemDetails problem = response.body();
        assertNotNull(problem);
        assertEquals("https://example.com/errors/invalid-argument", problem.type());
        assertEquals("Invalid Argument", problem.title());
        assertEquals(400, problem.status());
        assertEquals("Pagination size must be between 1 and 50", problem.detail());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @Test
    @DisplayName("ValidationExceptionHandler: プロパティパスのノード名が空の場合、パス文字列表現をフォールバックとして使用すること")
    @SuppressWarnings("unchecked")
    void testValidationExceptionHandlerFallbackPropertyPath() {
        ValidationExceptionHandler handler = new ValidationExceptionHandler();
        HttpRequest<?> request = HttpRequest.POST("/api/posts", "{}");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Path.Node blankNode = mock(Path.Node.class);

        when(blankNode.getName()).thenReturn("");
        when(path.iterator()).thenReturn(List.of(blankNode).iterator());
        when(path.toString()).thenReturn("request.fallbackField");
        when(violation.getPropertyPath()).thenReturn(path);
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
    @DisplayName("ドメイン例外階層: PostValidationException および PostStorageException が正しくインスタンス化されること")
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
