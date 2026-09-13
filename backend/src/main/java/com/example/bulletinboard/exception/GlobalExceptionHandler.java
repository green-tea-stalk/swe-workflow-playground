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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Global fallback exception handler translating unhandled exceptions into RFC 9457 Problem Details envelopes
 * localized according to incoming HTTP Accept-Language headers without leaking internal stack traces.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<ProblemDetails>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String PROBLEM_TYPE = "https://example.com/errors/internal-server-error";
    private static final String DEFAULT_PROBLEM_TITLE = "Internal Server Error";
    private static final String DEFAULT_PROBLEM_DETAIL = "An unexpected error occurred while processing the request.";
    private static final String MSG_KEY_TITLE = "error.internal.title";
    private static final String MSG_KEY_DETAIL = "error.internal.detail";

    private final LocaleResolver localeResolver;
    private final MessageLocalizationService messageLocalizationService;

    /**
     * Constructs a new GlobalExceptionHandler with required dependencies.
     *
     * @param localeResolver             resolver for extracting client locale from HTTP headers
     * @param messageLocalizationService service for resolving localized messages from bundles
     */
    public GlobalExceptionHandler(LocaleResolver localeResolver,
                                  MessageLocalizationService messageLocalizationService) {
        this.localeResolver = Objects.requireNonNull(localeResolver, "LocaleResolver must not be null");
        this.messageLocalizationService = Objects.requireNonNull(messageLocalizationService, "MessageLocalizationService must not be null");
    }

    /**
     * Handles an unhandled Throwable by translating it into a localized RFC 9457 500 Problem Details response
     * without leaking internal stack traces or database connection details.
     *
     * @param request   the incoming HTTP request (must not be null)
     * @param exception the caught Throwable (must not be null)
     * @return an HTTP 500 Internal Server Error response containing localized ProblemDetails
     * @throws NullPointerException if request or exception is null
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, Throwable exception) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");

        String sanitizedPath = request.getPath().replaceAll("[\r\n]", "_");
        LOG.error("Unhandled exception caught by global handler on path: {}", sanitizedPath, exception);

        Locale locale = localeResolver.resolveLocale(request);
        String title = messageLocalizationService.getMessageOrDefault(MSG_KEY_TITLE, locale, DEFAULT_PROBLEM_TITLE);
        String detail = messageLocalizationService.getMessageOrDefault(MSG_KEY_DETAIL, locale, DEFAULT_PROBLEM_DETAIL);

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                title,
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                detail,
                request.getPath(),
                List.of()
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .header("Content-Language", locale.getLanguage())
                .body(problem);
    }
}
