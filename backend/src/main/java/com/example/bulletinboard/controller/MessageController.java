package com.example.bulletinboard.controller;

import com.example.bulletinboard.dto.MessageCreateRequest;
import com.example.bulletinboard.dto.MessageResponse;
import com.example.bulletinboard.dto.PageResponse;
import com.example.bulletinboard.service.MessageService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * 掲示板メッセージのREST APIエンドポイントを提供するコントローラー。
 * メッセージの投稿（POST /api/messages）および逆時系列ページネーション一覧取得（GET /api/messages）を公開する。
 *
 * <p>契約仕様 (DbC: Design by Contract):
 * <ul>
 *   <li>事前条件: {@link #getMessages(int, int)} の引数 page &gt;= 0, 1 &lt;= size &lt;= 100 であること。</li>
 *   <li>事前条件: {@link #createMessage(MessageCreateRequest)} の引数 request は非nullかつバリデーション制約を満たすこと。</li>
 *   <li>事後条件: {@link #getMessages(int, int)} は 200 OK と投稿日時の降順（最新順）メッセージ一覧を返却すること。</li>
 *   <li>事後条件: {@link #createMessage(MessageCreateRequest)} は 201 Created と採番されたIDおよび作成日時を持つ応答を返却すること。</li>
 *   <li>不変条件: すべてのHTTP通信は application/json（エラー時は application/problem+json）でシリアライズされること。</li>
 * </ul>
 */
@Controller("/api/messages")
@Validated
public class MessageController {

    private final MessageService messageService;

    /**
     * ドメインサービス依存を注入してコントローラーを初期化する。
     *
     * @param messageService メッセージドメインサービス（非null）
     */
    public MessageController(MessageService messageService) {
        this.messageService = Objects.requireNonNull(messageService, "messageServiceは必須です");
    }

    /**
     * 指定されたページ番号および件数に従い、投稿日時の降順でメッセージ一覧を取得する。
     *
     * @param page 0開始のページインデックス（デフォルト: 0、0以上）
     * @param size 1ページあたりの件数（デフォルト: 50、1以上100以下）
     * @return 200 OK とページネーションされたメッセージ応答
     */
    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<PageResponse<MessageResponse>> getMessages(
            @QueryValue(defaultValue = "0") @Min(0) int page,
            @QueryValue(defaultValue = "50") @Min(1) @Max(MessageService.MAX_PAGE_SIZE) int size
    ) {
        PageResponse<MessageResponse> response = messageService.findMessages(page, size);
        return HttpResponse.ok(response);
    }

    /**
     * 新規メッセージを投稿・作成する。
     *
     * @param request 投稿リクエスト（非nullかつ制約準拠）
     * @return 201 Created と採番されたメッセージ応答
     */
    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<MessageResponse> createMessage(@Body @NotNull @Valid MessageCreateRequest request) {
        MessageResponse response = messageService.createMessage(request);
        return HttpResponse.created(response);
    }
}
