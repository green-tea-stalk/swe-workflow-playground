package com.example.bulletinboard.exception;

import com.example.bulletinboard.service.MessageLocalizationService;
import com.example.bulletinboard.util.LocaleResolver;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.validation.exceptions.ConstraintExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Global exception handler translating Jakarta Bean Validation errors into RFC 9457 Problem Details envelopes
 * localized according to incoming HTTP Accept-Language headers.
 */
@Singleton
@Replaces(ConstraintExceptionHandler.class)
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class ValidationExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<ProblemDetails>> {

    private static final String PROBLEM_TYPE = "https://example.com/errors/validation-failed";
    private static final String DEFAULT_PROBLEM_TITLE = "Validation Failed";
    private static final String DEFAULT_PROBLEM_DETAIL = "Input payload failed validation constraints.";
    private static final String MSG_KEY_TITLE = "error.validation.title";
    private static final String MSG_KEY_DETAIL = "error.validation.detail";

    private final LocaleResolver localeResolver;
    private final MessageLocalizationService messageLocalizationService;

    /**
     * Constructs a new ValidationExceptionHandler with required dependencies.
     *
     * @param localeResolver             resolver for extracting client locale from HTTP headers
     * @param messageLocalizationService service for resolving localized messages from bundles
     */
    public ValidationExceptionHandler(LocaleResolver localeResolver,
                                      MessageLocalizationService messageLocalizationService) {
        this.localeResolver = Objects.requireNonNull(localeResolver, "LocaleResolver must not be null");
        this.messageLocalizationService = Objects.requireNonNull(messageLocalizationService, "MessageLocalizationService must not be null");
    }

    /**
     * Handles a ConstraintViolationException by translating it into a localized RFC 9457 Problem Details response.
     *
     * @param request   the incoming HTTP request (must not be null)
     * @param exception the caught ConstraintViolationException (must not be null)
     * @return an HTTP 400 Bad Request response containing localized ProblemDetails
     * @throws NullPointerException if request or exception is null
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, ConstraintViolationException exception) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(exception, "Exception must not be null");

        Locale locale = localeResolver.resolveLocale(request);
        String title = messageLocalizationService.getMessageOrDefault(MSG_KEY_TITLE, locale, DEFAULT_PROBLEM_TITLE);
        String detail = messageLocalizationService.getMessageOrDefault(MSG_KEY_DETAIL, locale, DEFAULT_PROBLEM_DETAIL);

        List<InvalidParam> invalidParams = (exception.getConstraintViolations() == null)
                ? List.of()
                : exception.getConstraintViolations().stream()
                .filter(Objects::nonNull)
                .map(violation -> toInvalidParam(violation, locale))
                .distinct()
                .sorted(Comparator.comparing(InvalidParam::name).thenComparing(InvalidParam::reason))
                .toList();

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                title,
                HttpStatus.BAD_REQUEST.getCode(),
                detail,
                request.getPath(),
                invalidParams
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .header("Content-Language", locale.getLanguage())
                .body(problem);
    }

    private InvalidParam toInvalidParam(ConstraintViolation<?> violation, Locale locale) {
        String paramName = null;
        if (violation.getPropertyPath() != null) {
            for (Path.Node node : violation.getPropertyPath()) {
                paramName = node.getName();
            }
            if (paramName == null || paramName.isBlank()) {
                paramName = violation.getPropertyPath().toString();
            }
        }
        if (paramName == null || paramName.isBlank()) {
            paramName = "unknown";
        }

        String rawTemplate = violation.getMessageTemplate();
        String reason;

        if (rawTemplate != null && rawTemplate.startsWith("{") && rawTemplate.endsWith("}") && rawTemplate.length() > 2) {
            String code = rawTemplate.substring(1, rawTemplate.length() - 1);
            reason = messageLocalizationService.getMessageOrDefault(code, locale, violation.getMessage());
        } else if (rawTemplate != null && !rawTemplate.isBlank()) {
            reason = messageLocalizationService.getMessageOrDefault(rawTemplate, locale, violation.getMessage());
        } else {
            reason = violation.getMessage();
        }

        return new InvalidParam(paramName, reason != null ? reason : "");
    }
}
