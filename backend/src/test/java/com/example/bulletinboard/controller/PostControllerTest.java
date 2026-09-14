package com.example.bulletinboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.CreateReplyRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.dto.ReplyResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.exception.ProblemDetails;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.ReplyRepository;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Integration test suite verifying REST API controller contracts, status codes, headers, and RFC 9457 error envelopes.
 */
@MicronautTest(transactional = false)
@DisplayName("Integration tests for PostController REST endpoints")
class PostControllerTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    PostRepository postRepository;

    @Inject
    ReplyRepository replyRepository;

    private BlockingHttpClient client;

    @BeforeEach
    void setUp() {
        client = httpClient.toBlocking();
        replyRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/posts: should return 200 OK and guaranteed empty list with default parameters")
    void testListPostsDefaultReturnsEmptyPage() {
        HttpRequest<?> request = HttpRequest.GET("/api/posts");
        HttpResponse<PagedPostResponse> response = client.exchange(request, PagedPostResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        PagedPostResponse body = response.body();
        assertNotNull(body);
        assertNotNull(body.items(), "items must not be null when 0 posts exist");
        assertTrue(body.items().isEmpty(), "items must be empty list [] when 0 posts exist");
        assertEquals(0, body.page());
        assertEquals(50, body.size());
        assertEquals(0L, body.totalItems());
        assertEquals(0, body.totalPages());
    }

    @Test
    @DisplayName("POST /api/posts: should return 201 Created, Location header, and persisted entity on valid request")
    void testCreatePostSuccessfully() {
        CreatePostRequest requestPayload =
                new CreatePostRequest("  Alice  ", "alice@example.com", "  First Post  ", "  Hello World!  ");

        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);
        HttpResponse<PostResponse> response = client.exchange(request, PostResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        String location = response.header("Location");
        assertNotNull(location, "Location header must be present");

        PostResponse body = response.body();
        assertNotNull(body);
        assertNotNull(body.id());
        assertEquals("/api/posts/" + body.id(), location);
        assertEquals("Alice", body.name(), "Name must be trimmed");
        assertEquals("alice@example.com", body.email(), "Email must be trimmed");
        assertEquals("First Post", body.title(), "Title must be trimmed");
        assertEquals("Hello World!", body.message(), "Message must be trimmed");
        assertNotNull(body.createdAt(), "ISO 8601 UTC timestamp must be populated");

        HttpResponse<PagedPostResponse> feedResponse =
                client.exchange(HttpRequest.GET("/api/posts"), PagedPostResponse.class);
        assertEquals(1, feedResponse.body().items().size());
        assertEquals(body.id(), feedResponse.body().items().get(0).id());
    }

    @Test
    @DisplayName("POST /api/posts: should return 201 Created retaining null email when email is omitted")
    void testCreatePostWithNullEmail() {
        CreatePostRequest requestPayload = new CreatePostRequest("Bob", null, "Title Without Email", "Message Body");

        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);
        HttpResponse<PostResponse> response = client.exchange(request, PostResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        PostResponse body = response.body();
        assertNotNull(body);
        assertNull(body.email(), "Omitted email must remain null");
    }

    @ParameterizedTest(
            name = "Invalid blank required field: name=''{0}'', title=''{1}'', message=''{2}'', expectedField=''{3}''")
    @CsvSource({
        "'   ', 'Valid Title', 'Valid Message', 'name'",
        "'', 'Valid Title', 'Valid Message', 'name'",
        "'Valid Name', '   ', 'Valid Message', 'title'",
        "'Valid Name', '', 'Valid Message', 'title'",
        "'Valid Name', 'Valid Title', '   ', 'message'",
        "'Valid Name', 'Valid Title', '', 'message'"
    })
    @DisplayName(
            "POST /api/posts: should return 400 Bad Request with RFC 9457 ProblemDetails when required fields are blank")
    void testCreatePostRejectsBlankFields(String name, String title, String message, String expectedField) {
        CreatePostRequest requestPayload = new CreatePostRequest(name, null, title, message);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent(), "RFC 9457 ProblemDetails body must be present");

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams(), "invalid_params must not be null");
        assertTrue(
                problem.invalidParams().stream().anyMatch(p -> expectedField.equals(p.name())),
                "invalid_params must contain violation for expected field: " + expectedField);
    }

    @Test
    @DisplayName("POST /api/posts: should return 400 Bad Request with email violation when email format is invalid")
    void testCreatePostRejectsInvalidEmailFormat() {
        CreatePostRequest requestPayload = new CreatePostRequest("Alice", "not-an-email", "Title", "Message");
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        boolean hasEmailViolation =
                problem.invalidParams().stream().anyMatch(param -> param.name().contains("email"));
        assertTrue(hasEmailViolation, "invalid_params must include violation for email");
    }

    @ParameterizedTest(name = "Invalid pagination argument: page={0}, size={1}")
    @CsvSource({"-1, 50", "0, 0", "0, -5", "0, 51", "0, 100"})
    @DisplayName(
            "GET /api/posts: should return 400 Bad Request with RFC 9457 ProblemDetails when page or size is out of bounds")
    void testListPostsRejectsInvalidPagination(int page, int size) {
        HttpRequest<?> request = HttpRequest.GET("/api/posts?page=" + page + "&size=" + size);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(
                        request, Argument.of(PagedPostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());
        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(!problem.invalidParams().isEmpty());
    }

    @ParameterizedTest(
            name = "Exceeded max length field: nameLen={0}, titleLen={1}, messageLen={2}, expectedField=''{3}''")
    @CsvSource({"51, 10, 10, 'name'", "10, 101, 10, 'title'", "10, 10, 4001, 'message'"})
    @DisplayName("POST /api/posts: should return 400 Bad Request when required field exceeds maximum allowed length")
    void testCreatePostRejectsExceededLength(int nameLen, int titleLen, int messageLen, String expectedField) {
        String name = "A".repeat(nameLen);
        String title = "T".repeat(titleLen);
        String message = "M".repeat(messageLen);

        CreatePostRequest requestPayload = new CreatePostRequest(name, null, title, message);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());
        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(
                problem.invalidParams().stream().anyMatch(p -> expectedField.equals(p.name())),
                "invalid_params must contain length violation for expected field: " + expectedField);
    }

    @Test
    @DisplayName("GET /api/posts: should return paginated posts matching custom page and size parameters")
    void testListPostsWithCustomPagination() {
        for (int i = 1; i <= 3; i++) {
            postRepository.save(new PostEntity(
                    null,
                    "User" + i,
                    null,
                    "Title" + i,
                    "Msg" + i,
                    LocalDateTime.now().minusMinutes(i)));
        }

        HttpRequest<?> request = HttpRequest.GET("/api/posts?page=1&size=2");
        HttpResponse<PagedPostResponse> response = client.exchange(request, PagedPostResponse.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        PagedPostResponse body = response.body();
        assertNotNull(body);
        assertEquals(1, body.page());
        assertEquals(2, body.size());
        assertEquals(3L, body.totalItems());
        assertEquals(2, body.totalPages());
        assertEquals(1, body.items().size());
    }

    @Test
    @DisplayName("PostController constructor: should throw NullPointerException when PostService is null")
    void testControllerRejectsNullService() {
        assertThrows(NullPointerException.class, () -> new PostController(null));
    }

    @Test
    @DisplayName("POST /api/posts: should return localized Japanese ProblemDetails when Accept-Language is ja")
    void testCreatePostReturnsJapaneseProblemDetailsWhenAcceptLanguageIsJa() {
        CreatePostRequest requestPayload = new CreatePostRequest("", null, "", "");
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload).header("Accept-Language", "ja");

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("ja", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("入力値検証エラー", problem.title());
        assertEquals("入力内容に不備があります。制約条件を確認してください。", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());

        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "name".equals(p.name()) && "名前を入力してください".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "title".equals(p.name()) && "タイトルを入力してください".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "message".equals(p.name()) && "メッセージ本文を入力してください".equals(p.reason())));
    }

    @Test
    @DisplayName("POST /api/posts: should return localized English ProblemDetails when Accept-Language is en")
    void testCreatePostReturnsEnglishProblemDetailsWhenAcceptLanguageIsEn() {
        CreatePostRequest requestPayload = new CreatePostRequest("", null, "", "");
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload).header("Accept-Language", "en");

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams());

        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "name".equals(p.name()) && "Name must not be blank".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "title".equals(p.name()) && "Title must not be blank".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "message".equals(p.name()) && "Message must not be blank".equals(p.reason())));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/replies: should return 201 Created and Location header on valid request")
    void testCreateReplySuccessfully() {
        PostEntity parent = postRepository.save(new PostEntity(
                null, "Alice", "alice@example.com", "Parent Post", "Parent content", LocalDateTime.now()));

        CreateReplyRequest requestPayload =
                new CreateReplyRequest("  Bob  ", "bob@example.com", "  Great discussion!  ");
        HttpRequest<?> request = HttpRequest.POST("/api/posts/" + parent.id() + "/replies", requestPayload);
        HttpResponse<ReplyResponse> response = client.exchange(request, ReplyResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        String location = response.header("Location");
        assertNotNull(location, "Location header must be present");

        ReplyResponse body = response.body();
        assertNotNull(body);
        assertNotNull(body.id());
        assertEquals("/api/posts/" + parent.id() + "/replies/" + body.id(), location);
        assertEquals(parent.id(), body.postId());
        assertEquals("Bob", body.name(), "Name must be trimmed");
        assertEquals("bob@example.com", body.email(), "Email must be trimmed");
        assertEquals("Great discussion!", body.message(), "Message must be trimmed");
        assertNotNull(body.createdAt(), "ISO 8601 UTC timestamp must be populated");

        // Verify that feed retrieval embeds the newly created reply
        HttpResponse<PagedPostResponse> feedResponse =
                client.exchange(HttpRequest.GET("/api/posts"), PagedPostResponse.class);
        assertEquals(1, feedResponse.body().items().size());
        PostResponse postWithReply = feedResponse.body().items().get(0);
        assertEquals(1, postWithReply.replies().size(), "Feed item must contain 1 reply");
        assertEquals(body.id(), postWithReply.replies().get(0).id());
        assertEquals("Bob", postWithReply.replies().get(0).name());
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/replies: should succeed with null optional email")
    void testCreateReplyWithNullEmail() {
        PostEntity parent = postRepository.save(new PostEntity(
                null, "Alice", "alice@example.com", "Parent Post", "Parent content", LocalDateTime.now()));

        CreateReplyRequest requestPayload = new CreateReplyRequest("Bob", null, "Reply without email");
        HttpRequest<?> request = HttpRequest.POST("/api/posts/" + parent.id() + "/replies", requestPayload);
        HttpResponse<ReplyResponse> response = client.exchange(request, ReplyResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        ReplyResponse body = response.body();
        assertNotNull(body);
        assertNotNull(body.id());
        assertNull(body.email(), "Omitted email must remain null");
        assertEquals("Bob", body.name());
        assertEquals("Reply without email", body.message());
    }

    @Test
    @DisplayName(
            "POST /api/posts/{postId}/replies: should return 400 Bad Request with ProblemDetails on validation error")
    void testCreateReplyValidationFailure() {
        PostEntity parent = postRepository.save(
                new PostEntity(null, "Alice", null, "Parent Post", "Parent content", LocalDateTime.now()));

        CreateReplyRequest invalidPayload = new CreateReplyRequest("", "invalid-email", "");
        HttpRequest<?> request = HttpRequest.POST("/api/posts/" + parent.id() + "/replies", invalidPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(ReplyResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts/" + parent.id() + "/replies", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "name".equals(p.name()) && "Name must not be blank".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p ->
                        "email".equals(p.name()) && "Email must be a well-formed email address".equals(p.reason())));
        assertTrue(problem.invalidParams().stream()
                .anyMatch(p -> "message".equals(p.name()) && "Message must not be blank".equals(p.reason())));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/replies: should return 404 Not Found when target post does not exist")
    void testCreateReplyParentPostNotFound() {
        CreateReplyRequest requestPayload = new CreateReplyRequest("Bob", null, "Replying to missing post");
        HttpRequest<?> request = HttpRequest.POST("/api/posts/999999/replies", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(ReplyResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/post-not-found", problem.type());
        assertEquals("Post Not Found", problem.title());
        assertEquals(404, problem.status());
        assertEquals("Parent post with ID 999999 was not found.", problem.detail());
        assertEquals("/api/posts/999999/replies", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/replies: should return localized 404 Not Found when Accept-Language is ja")
    void testCreateReplyParentPostNotFoundLocalizedJa() {
        CreateReplyRequest requestPayload = new CreateReplyRequest("Bob", null, "Replying to missing post");
        HttpRequest<?> request =
                HttpRequest.POST("/api/posts/888888/replies", requestPayload).header("Accept-Language", "ja");

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(ReplyResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("ja", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/post-not-found", problem.type());
        assertEquals("対象の投稿が見つかりません", problem.title());
        assertEquals(404, problem.status());
        assertEquals("ID 888888 の親投稿が見つかりませんでした。", problem.detail());
        assertEquals("/api/posts/888888/replies", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(problem.invalidParams().isEmpty());
    }

    @ParameterizedTest(name = "invalid postId: {0}")
    @CsvSource({"0", "-1", "-100"})
    @DisplayName("POST /api/posts/{postId}/replies: non-positive postId should return 400 Bad Request")
    void testCreateReplyWithInvalidPostId(long invalidPostId) {
        CreateReplyRequest requestPayload = new CreateReplyRequest("Bob", null, "Valid message");
        HttpRequest<?> request = HttpRequest.POST("/api/posts/" + invalidPostId + "/replies", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(ReplyResponse.class), Argument.of(ProblemDetails.class)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(
                "application/problem+json",
                ex.getResponse().getContentType().map(Object::toString).orElse(""));
        assertEquals("en", ex.getResponse().getHeaders().get("Content-Language"));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent(), "RFC 9457 ProblemDetails body must be present");

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals("Input payload failed validation constraints.", problem.detail());
        assertEquals(400, problem.status());
        assertEquals("/api/posts/" + invalidPostId + "/replies", problem.instance());
        assertNotNull(problem.invalidParams());
        assertTrue(
                problem.invalidParams().stream().anyMatch(p -> p.name().contains("postId")),
                "invalid_params must contain violation for postId");
    }
}
