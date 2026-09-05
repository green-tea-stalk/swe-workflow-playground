package com.example.bulletinboard.repository;

import com.example.bulletinboard.entity.MessageEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

/**
 * 掲示板メッセージの永続化操作を担うリポジトリインターフェース。
 * Micronaut Data JDBC により、AOT（事前コンパイル）でSQLクエリが生成される。
 *
 * <p>契約仕様 (DbC: Design by Contract):
 * <ul>
 *   <li>事前条件: {@link #save(MessageEntity)} に渡すエンティティは name, title, message, createdAt が非nullであること。</li>
 *   <li>事後条件: {@link #save(MessageEntity)} は自動採番された主キーIDを含む永続化済みエンティティを返却すること。</li>
 *   <li>事後条件: {@link #findAll(Pageable)} は指定された Pageable（created_at DESC）に従いページネーションされた結果を返却すること。</li>
 *   <li>不変条件: データベースの messages テーブル制約（NOT NULL, カラム長）とエンティティ制約が整合していること。</li>
 * </ul>
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface MessageRepository extends PageableRepository<MessageEntity, Long> {

    /**
     * 指定されたページネーションおよびソート条件に従ってメッセージ一覧を取得する。
     *
     * @param pageable ページネーションおよびソート指定（非null）
     * @return 指定されたページ内のメッセージ一覧を含むページオブジェクト
     */
    @Override
    Page<MessageEntity> findAll(Pageable pageable);

    /**
     * メッセージエンティティをデータベースに永続化する。
     *
     * @param entity 永続化対象のエンティティ（name, title, message, createdAt は非null必須）
     * @param <S> エンティティの具象型
     * @return 採番された主キーIDを含む永続化済みエンティティ
     */
    @Override
    <S extends MessageEntity> S save(S entity);
}
