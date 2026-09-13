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
 * Global exception handler translating IllegalArgumentException into RFC 9457 Problem Details envelopes
 * localized according to incoming HTTP Accept-Language headers.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException, HttpResponse<ProblemDetails>> {

    private static final String PROBLEM_TYPE = "https://example.com/errors/invalid-argument";
    private static final String DEFAULT_PROBLEM_TITLE = "Invalid Argument";
    private static final String MSG_KEY_TITLE = "error.invalid_arg.title";

    private final LocaleResolver localeResolver;
    private final MessageLocalizationService messageLocalizationService;

    /**
     * Constructs a new IllegalArgumentExceptionHandler with required dependencies.
     *
     * @param localeResolver             resolver for extracting client locale from HTTP headers
     * @param messageLocalizationService service for resolving localized messages from bundles
     */
    public IllegalArgumentExceptionHandler(LocaleResolver localeResolver,
                                           MessageLocalizationService messageLocalizationService) {
        this.localeResolver = Objects.requireNonNull(localeResolver, "LocaleResolver must not be null");
        this.messageLocalizationService = Objects.requireNonNull(messageLocalizationService, "MessageLocalizationService must not be null");
    }

    /**
     * Handles an IllegalArgumentException by translating it into a localized RFC 9457 Problem Details response.
     *
     * @param request   the incoming HTTP request (must not be null)
     * @param exception the caught IllegalArgumentException (must not be null)
     * @return an HTTP 400 Bad Request response containing localized ProblemDetails
     * @throws NullPointerException if request or exception is null
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, IllegalArgumentException exception) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");

        Locale locale = localeResolver.resolveLocale(request);
        String title = messageLocalizationService.getMessageOrDefault(MSG_KEY_TITLE, locale, DEFAULT_PROBLEM_TITLE);

        String rawMessage = exception.getMessage();
        String detail;
        if (rawMessage == null || rawMessage.isBlank() || rawMessage.contains("com.example.") || rawMessage.contains("Exception")) {
            detail = title;
        } else {
            detail = rawMessage;
        }

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                title,
                HttpStatus.BAD_REQUEST.getCode(),
                detail,
                request.getPath(),
                List.of()
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .header("Content-Language", locale.getLanguage())
                .body(problem);
    }
}
