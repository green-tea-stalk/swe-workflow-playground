package com.example.bulletinboard.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * システム内部の予期せぬ実行時例外を捕捉し、スタックトレースや内部構造を秘匿した上で
 * RFC 9457 形式の 500 Internal Server Error 応答に変換する汎用例外ハンドラー。
 */
@Singleton
@Produces({GlobalExceptionHandler.MEDIA_TYPE_PROBLEM_JSON, MediaType.APPLICATION_JSON})
@Requires(classes = {Throwable.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<ProblemDetails>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final String TYPE_URI = "https://api.bulletin-board.local/errors/internal-server-error";
    public static final String TITLE = "Internal Server Error";
    public static final String DEFAULT_DETAIL = "内部サーバーエラーが発生しました。時間をおいて再試行してください。";
    public static final String MEDIA_TYPE_PROBLEM_JSON = "application/problem+json";

    /**
     * 予期せぬ例外を捕捉し、内部情報を隠蔽した RFC 9457 形式の 500 Internal Server Error レスポンスを構築する。
     *
     * @param request HTTPリクエスト情報（null許容フォールバック）
     * @param exception 発生した実行時例外
     * @return RFC 9457 Problem Details を含む 500 Internal Server Error 応答
     */
    @Override
    public HttpResponse<ProblemDetails> handle(HttpRequest request, Throwable exception) {
        LOG.error("予期せぬ内部エラーが発生しました: requestPath={}", request != null ? request.getPath() : "unknown", exception);

        String instance = request != null ? request.getPath() : "";

        ProblemDetails problemDetails = new ProblemDetails(
                TYPE_URI,
                TITLE,
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                DEFAULT_DETAIL,
                instance,
                null
        );

        return HttpResponse.<ProblemDetails>status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.of(MEDIA_TYPE_PROBLEM_JSON))
                .body(problemDetails);
    }
}
