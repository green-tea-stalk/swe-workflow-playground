package com.example.bulletinboard.repository;

import com.example.bulletinboard.entity.ReplyEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

/**
 * Data access repository for managing {@link ReplyEntity} persistence in MySQL 8.4.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface ReplyRepository extends CrudRepository<ReplyEntity, Long> {

    /**
     * Retrieves all replies belonging to a specific post in ascending chronological order.
     *
     * @param postId the parent post database identifier
     * @return a non-null list of replies ordered oldest first (empty list if none exist)
     */
    List<ReplyEntity> findByPostIdOrderByCreatedAtAsc(Long postId);

    /**
     * Retrieves all replies for a given collection of post IDs in ascending chronological order.
     * Enables single-query batch fetching to eliminate N+1 round trips during feed rendering.
     *
     * @param postIds list of parent post identifiers
     * @return a non-null list of replies ordered oldest first (empty list if none exist)
     */
    List<ReplyEntity> findByPostIdInOrderByCreatedAtAsc(List<Long> postIds);
}
