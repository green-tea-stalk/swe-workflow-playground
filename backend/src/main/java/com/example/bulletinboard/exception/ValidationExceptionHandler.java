package com.example.bulletinboard.exception;

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

/**
 * Global exception handler translating Jakarta Bean Validation errors into RFC 9457 Problem Details envelopes.
 */
@Singleton
@Replaces(ConstraintExceptionHandler.class)
@Produces(MediaType.APPLICATION_JSON_PROBLEM)
public class ValidationExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<ProblemDetails>> {

    private static final String PROBLEM_TYPE = "https://example.com/errors/validation-failed";
    private static final String PROBLEM_TITLE = "Validation Failed";
    private static final String PROBLEM_DETAIL = "Input payload failed validation constraints.";

    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, ConstraintViolationException exception) {
        List<InvalidParam> invalidParams = exception.getConstraintViolations().stream()
                .map(this::toInvalidParam)
                .sorted(Comparator.comparing(InvalidParam::name))
                .toList();

        ProblemDetails problem = new ProblemDetails(
                PROBLEM_TYPE,
                PROBLEM_TITLE,
                HttpStatus.BAD_REQUEST.getCode(),
                PROBLEM_DETAIL,
                request.getPath(),
                invalidParams
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_PROBLEM_TYPE)
                .body(problem);
    }

    private InvalidParam toInvalidParam(ConstraintViolation<?> violation) {
        String paramName = null;
        for (Path.Node node : violation.getPropertyPath()) {
            paramName = node.getName();
        }
        if (paramName == null || paramName.isBlank()) {
            paramName = violation.getPropertyPath().toString();
        }

        return new InvalidParam(paramName, violation.getMessage());
    }
}
