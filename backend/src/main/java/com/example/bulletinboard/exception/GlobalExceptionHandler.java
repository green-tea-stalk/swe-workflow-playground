package com.example.bulletinboard.exception;

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

/**
 * Global fallback exception handler translating unhandled exceptions into RFC 9457 Problem Details envelopes
 * without leaking internal stack traces.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<ProblemDetails>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String PROBLEM_TYPE = "https://example.com/errors/internal-server-error";
    private static final String PROBLEM_TITLE = "Internal Server Error";
    private static final String PROBLEM_DETAIL = "An unexpected error occurred while processing the request.";

    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, Throwable exception) {
        LOG.error("Unhandled exception caught by global handler on path: {}", request.getPath(), exception);

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                PROBLEM_TITLE,
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                PROBLEM_DETAIL,
                request.getPath(),
                List.of()
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .body(problem);
    }
}
