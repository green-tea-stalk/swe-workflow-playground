package com.example.bulletinboard.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Jakarta Bean Validation 由来の {@link ConstraintViolationException} を捕捉し、
 * RFC 9457 形式の 400 Bad Request 応答に変換する例外ハンドラー。
 */
@Singleton
@Produces({ValidationExceptionHandler.MEDIA_TYPE_PROBLEM_JSON, MediaType.APPLICATION_JSON})
@Requires(classes = {ConstraintViolationException.class, ExceptionHandler.class})
public class ValidationExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<ProblemDetails>> {

    public static final String TYPE_URI = "https://api.bulletin-board.local/errors/invalid-request";
    public static final String TITLE = "Bad Request";
    public static final String MEDIA_TYPE_PROBLEM_JSON = "application/problem+json";

    /**
     * バリデーション例外を捕捉し、RFC 9457 形式の 400 Bad Request レスポンスを構築する。
     *
     * @param request HTTPリクエスト情報（null許容フォールバック）
     * @param exception 発生した制約違反例外
     * @return RFC 9457 Problem Details を含む 400 Bad Request 応答
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, ConstraintViolationException exception) {
        List<InvalidParam> invalidParams = new ArrayList<>();

        if (exception != null && exception.getConstraintViolations() != null) {
            for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                String propertyName = extractPropertyName(violation.getPropertyPath());
                invalidParams.add(new InvalidParam(propertyName, violation.getMessage()));
            }
        }

        String detail = String.format("入力値検証に失敗しました（%d件のエラー）。", invalidParams.size());
        String instance = request != null ? request.getPath() : "";

        ProblemDetails problemDetails = new ProblemDetails(
                TYPE_URI,
                TITLE,
                HttpStatus.BAD_REQUEST.getCode(),
                detail,
                instance,
                invalidParams
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.of(MEDIA_TYPE_PROBLEM_JSON))
                .body(problemDetails);
    }

    private String extractPropertyName(Path path) {
        if (path == null) {
            return "unknown";
        }
        String pathString = path.toString();
        int lastDotIndex = pathString.lastIndexOf('.');
        return lastDotIndex != -1 ? pathString.substring(lastDotIndex + 1) : pathString;
    }
}
