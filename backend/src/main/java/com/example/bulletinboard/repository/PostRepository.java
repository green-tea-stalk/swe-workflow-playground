package com.example.bulletinboard.repository;

import com.example.bulletinboard.entity.PostEntity;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

/**
 * Data access repository for managing {@link PostEntity} persistence in MySQL 8.4.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface PostRepository extends PageableRepository<PostEntity, Long> {
}
