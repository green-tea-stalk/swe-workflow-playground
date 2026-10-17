package com.example.bulletinboard.exception;

import com.example.bulletinboard.service.MessageLocalizationService;
import com.example.bulletinboard.util.LocaleResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Global exception handler translating {@link PostNotFoundException} into RFC 9457 Problem Details envelopes
 * localized according to incoming HTTP Accept-Language headers.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class PostNotFoundExceptionHandler
        implements ExceptionHandler<PostNotFoundException, HttpResponse<ProblemDetails>> {

    private static final String PROBLEM_TYPE = "https://example.com/errors/post-not-found";
    private static final String DEFAULT_PROBLEM_TITLE = "Post Not Found";
    private static final String MSG_KEY_TITLE = "error.post_not_found.title";
    private static final String MSG_KEY_DETAIL = "error.post_not_found.detail";

    private final LocaleResolver localeResolver;
    private final MessageLocalizationService messageLocalizationService;

    /**
     * Constructs a new PostNotFoundExceptionHandler with required dependencies.
     *
     * @param localeResolver             resolver for extracting client locale from HTTP headers
     * @param messageLocalizationService service for resolving localized messages from bundles
     */
    public PostNotFoundExceptionHandler(
            LocaleResolver localeResolver, MessageLocalizationService messageLocalizationService) {
        this.localeResolver = Objects.requireNonNull(localeResolver, "LocaleResolver must not be null");
        this.messageLocalizationService =
                Objects.requireNonNull(messageLocalizationService, "MessageLocalizationService must not be null");
    }

    /**
     * Handles a PostNotFoundException by translating it into a localized RFC 9457 404 Problem Details response.
     *
     * @param request   the incoming HTTP request (must not be null)
     * @param exception the caught PostNotFoundException (must not be null)
     * @return an HTTP 404 Not Found response containing localized ProblemDetails
     * @throws NullPointerException if request or exception is null
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, PostNotFoundException exception) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");

        Locale locale = localeResolver.resolveLocale(request);
        String title = messageLocalizationService.getMessageOrDefault(MSG_KEY_TITLE, locale, DEFAULT_PROBLEM_TITLE);
        String defaultDetail = "Parent post with ID " + exception.getPostId() + " was not found.";
        String detail = messageLocalizationService.getMessageOrDefault(
                MSG_KEY_DETAIL, locale, defaultDetail, String.valueOf(exception.getPostId()));

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE, title, HttpStatus.NOT_FOUND.getCode(), detail, request.getPath(), List.of());

        return HttpResponse.<ProblemDetails>status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .header("Content-Language", locale.getLanguage())
                .body(problem);
    }
}
