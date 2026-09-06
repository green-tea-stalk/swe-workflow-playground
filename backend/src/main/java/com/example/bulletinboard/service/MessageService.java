package com.example.bulletinboard.service;

import com.example.bulletinboard.dto.MessageCreateRequest;
import com.example.bulletinboard.dto.MessageResponse;
import com.example.bulletinboard.dto.PageResponse;
import com.example.bulletinboard.entity.MessageEntity;
import com.example.bulletinboard.exception.DatabaseAccessException;
import com.example.bulletinboard.repository.MessageRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * 掲示板メッセージのドメインロジックおよび永続化連携を統括するアプリケーションサービス。
 * トランザクション境界の管理、入力値のサニタイズ（トリム・null正規化）、および逆時系列ページネーション検索を担う。
 *
 * <p>契約仕様 (DbC: Design by Contract):
 * <ul>
 *   <li>事前条件: {@link #createMessage(MessageCreateRequest)} の引数 request は非nullかつバリデーション制約を満たすこと。</li>
 *   <li>事前条件: {@link #findMessages(int, int)} の引数 page &gt;= 0, 1 &lt;= size &lt;= {@value #MAX_PAGE_SIZE} であること。</li>
 *   <li>事後条件: {@link #createMessage(MessageCreateRequest)} はトランザクション境界内で実行され、入力値を正規化した上で保存し、採番されたIDとUTC作成日時を持つ応答を返却すること。永続化失敗時はロールバックして {@link DatabaseAccessException} を伝播すること。</li>
 *   <li>事後条件: {@link #findMessages(int, int)} は投稿日時の降順（createdAt DESC, id DESC）でソートされたページネーション応答を返却すること。問い合わせ失敗時は {@link DatabaseAccessException} を伝播すること。</li>
 *   <li>不変条件: 永続化層へ渡されるエンティティの name, title, message はnullまたは空白文字のみであってはならない。</li>
 * </ul>
 */
@Singleton
public class MessageService {

    /**
     * 1ページあたりに要求可能な最大取得件数。
     * 大量データ取得によるヒープ枯渇およびサービス妨害（DoS）を防止するための上限値。
     */
    public static final int MAX_PAGE_SIZE = 100;

    private final MessageRepository messageRepository;

    /**
     * リポジトリ依存を注入してサービスを初期化する。
     *
     * @param messageRepository メッセージ永続化リポジトリ
     */
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = Objects.requireNonNull(messageRepository, "messageRepositoryは必須です");
    }

    /**
     * 新規メッセージをトランザクション内で作成・永続化する。
     *
     * @param request 投稿リクエスト（非nullかつ制約準拠）
     * @return 永続化されたメッセージ応答リソース
     * @throws NullPointerException requestがnullの場合
     * @throws DatabaseAccessException データベースへの永続化操作が失敗した場合
     */
    @Transactional
    public MessageResponse createMessage(@NotNull @Valid MessageCreateRequest request) {
        Objects.requireNonNull(request, "requestは非nullでなければなりません");

        try {
            MessageEntity entity = new MessageEntity(
                    null,
                    request.name(),
                    request.email(),
                    request.title(),
                    request.message(),
                    Instant.now()
            );

            MessageEntity saved = messageRepository.save(entity);
            return MessageResponse.fromEntity(saved);
        } catch (Exception e) {
            throw new DatabaseAccessException("データベースへのメッセージ保存中にエラーが発生しました", e);
        }
    }

    /**
     * 指定されたページ番号および件数に従い、投稿日時の降順（最新順）でメッセージ一覧を取得する。
     *
     * @param page 0開始のページインデックス（0以上）
     * @param size 1ページあたりの件数（1以上100以下）
     * @return ページネーションされたメッセージ応答封筒
     * @throws IllegalArgumentException pageが0未満、またはsizeが1未満もしくは100超過の場合
     * @throws DatabaseAccessException データベースからのデータ取得操作が失敗した場合
     */
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> findMessages(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("pageは0以上でなければなりません: " + page);
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("sizeは1以上%d以下でなければなりません: %d", MAX_PAGE_SIZE, size)
            );
        }

        try {
            Pageable pageable = Pageable.from(page, size, Sort.of(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            ));
            Page<MessageEntity> entityPage = messageRepository.findAll(pageable);

            return PageResponse.fromPage(entityPage, MessageResponse::fromEntity);
        } catch (Exception e) {
            throw new DatabaseAccessException("データベースからのメッセージ取得中にエラーが発生しました", e);
        }
    }
}
