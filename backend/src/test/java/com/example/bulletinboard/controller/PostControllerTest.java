package com.example.bulletinboard.controller;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.exception.ProblemDetails;
import com.example.bulletinboard.repository.PostRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private BlockingHttpClient client;

    @BeforeEach
    void setUp() {
        client = httpClient.toBlocking();
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
        CreatePostRequest requestPayload = new CreatePostRequest(
                "  Alice  ",
                "alice@example.com",
                "  First Post  ",
                "  Hello World!  "
        );

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

        // Verify newly created post is reflected in subsequent GET feed
        HttpResponse<PagedPostResponse> feedResponse = client.exchange(HttpRequest.GET("/api/posts"), PagedPostResponse.class);
        assertEquals(1, feedResponse.body().items().size());
        assertEquals(body.id(), feedResponse.body().items().get(0).id());
    }

    @Test
    @DisplayName("POST /api/posts: should return 201 Created retaining null email when email is omitted")
    void testCreatePostWithNullEmail() {
        CreatePostRequest requestPayload = new CreatePostRequest(
                "Bob",
                null,
                "Title Without Email",
                "Message Body"
        );

        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);
        HttpResponse<PostResponse> response = client.exchange(request, PostResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        PostResponse body = response.body();
        assertNotNull(body);
        assertNull(body.email(), "Omitted email must remain null");
    }

    @ParameterizedTest(name = "Invalid blank required field: name=''{0}'', title=''{1}'', message=''{2}''")
    @CsvSource({
            "'   ', 'Valid Title', 'Valid Message'",
            "'', 'Valid Title', 'Valid Message'",
            "'Valid Name', '   ', 'Valid Message'",
            "'Valid Name', '', 'Valid Message'",
            "'Valid Name', 'Valid Title', '   '",
            "'Valid Name', 'Valid Title', ''"
    })
    @DisplayName("POST /api/posts: should return 400 Bad Request with RFC 9457 ProblemDetails when required fields are blank")
    void testCreatePostRejectsBlankFields(String name, String title, String message) {
        CreatePostRequest requestPayload = new CreatePostRequest(name, null, title, message);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("application/problem+json", ex.getResponse().getContentType().map(Object::toString).orElse(""));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent(), "RFC 9457 ProblemDetails body must be present");

        ProblemDetails problem = problemOpt.get();
        assertEquals("https://example.com/errors/validation-failed", problem.type());
        assertEquals("Validation Failed", problem.title());
        assertEquals(400, problem.status());
        assertEquals("/api/posts", problem.instance());
        assertNotNull(problem.invalidParams(), "invalid_params must not be null");
        assertTrue(!problem.invalidParams().isEmpty(), "invalid_params must contain validation violation entries");
    }

    @Test
    @DisplayName("POST /api/posts: should return 400 Bad Request with email violation when email format is invalid")
    void testCreatePostRejectsInvalidEmailFormat() {
        CreatePostRequest requestPayload = new CreatePostRequest(
                "Alice",
                "not-an-email",
                "Title",
                "Message"
        );
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());

        ProblemDetails problem = problemOpt.get();
        boolean hasEmailViolation = problem.invalidParams().stream()
                .anyMatch(param -> param.name().contains("email"));
        assertTrue(hasEmailViolation, "invalid_params must include violation for email");
    }

    @ParameterizedTest(name = "Invalid pagination argument: page={0}, size={1}")
    @CsvSource({
            "-1, 50",
            "0, 0",
            "0, -5",
            "0, 51",
            "0, 100"
    })
    @DisplayName("GET /api/posts: should return 400 Bad Request with RFC 9457 ProblemDetails when page or size is out of bounds")
    void testListPostsRejectsInvalidPagination(int page, int size) {
        HttpRequest<?> request = HttpRequest.GET("/api/posts?page=" + page + "&size=" + size);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PagedPostResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("application/problem+json", ex.getResponse().getContentType().map(Object::toString).orElse(""));

        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());
        ProblemDetails problem = problemOpt.get();
        assertEquals(400, problem.status());
    }

    @ParameterizedTest(name = "Exceeded max length field: nameLen={0}, titleLen={1}, messageLen={2}")
    @CsvSource({
            "51, 10, 10",
            "10, 101, 10",
            "10, 10, 4001"
    })
    @DisplayName("POST /api/posts: should return 400 Bad Request when required field exceeds maximum allowed length")
    void testCreatePostRejectsExceededLength(int nameLen, int titleLen, int messageLen) {
        String name = "A".repeat(nameLen);
        String title = "T".repeat(titleLen);
        String message = "M".repeat(messageLen);

        CreatePostRequest requestPayload = new CreatePostRequest(name, null, title, message);
        HttpRequest<?> request = HttpRequest.POST("/api/posts", requestPayload);

        HttpClientResponseException ex = assertThrows(
                HttpClientResponseException.class,
                () -> client.exchange(request, Argument.of(PostResponse.class), Argument.of(ProblemDetails.class))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        Optional<ProblemDetails> problemOpt = ex.getResponse().getBody(ProblemDetails.class);
        assertTrue(problemOpt.isPresent());
        assertEquals(400, problemOpt.get().status());
    }

    @Test
    @DisplayName("GET /api/posts: should return paginated posts matching custom page and size parameters")
    void testListPostsWithCustomPagination() {
        for (int i = 1; i <= 3; i++) {
            postRepository.save(new PostEntity(null, "User" + i, null, "Title" + i, "Msg" + i, LocalDateTime.now().minusMinutes(i)));
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
}
