package com.example.bulletinboard.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Global exception handler translating IllegalArgumentException into RFC 9457 Problem Details envelopes.
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException, HttpResponse<ProblemDetails>> {

    private static final String PROBLEM_TYPE = "https://example.com/errors/invalid-argument";
    private static final String PROBLEM_TITLE = "Invalid Argument";

    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, IllegalArgumentException exception) {
        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                PROBLEM_TITLE,
                HttpStatus.BAD_REQUEST.getCode(),
                exception.getMessage(),
                request.getPath(),
                List.of()
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .body(problem);
    }
}
