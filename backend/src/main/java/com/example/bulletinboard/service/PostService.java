package com.example.bulletinboard.service;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.repository.PostRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Domain service coordinating bulletin board post creation, business validation, and paginated feed retrieval.
 */
@Singleton
public class PostService {

    private final PostRepository postRepository;
    private final Clock clock;

    /**
     * Constructs a new PostService instance.
     *
     * @param postRepository the underlying database repository
     */
    public PostService(PostRepository postRepository) {
        this(postRepository, Clock.systemUTC());
    }

    /**
     * Package-private constructor enabling deterministic time injection during tests.
     *
     * @param postRepository the underlying database repository
     * @param clock          the clock providing current UTC timestamps
     */
    PostService(PostRepository postRepository, Clock clock) {
        this.postRepository = Objects.requireNonNull(postRepository, "postRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Retrieves a paginated list of posts in reverse chronological order (newest first).
     *
     * @param page zero-based page index (must be &gt;= 0)
     * @param size maximum number of items per page (must be between 1 and 50)
     * @return a guaranteed non-null {@link PagedPostResponse} containing items or an empty list
     * @throws IllegalArgumentException if page or size violates preconditions
     */
    public PagedPostResponse getPagedPosts(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative: " + page);
        }
        if (size < 1 || size > 50) {
            throw new IllegalArgumentException("Page size must be between 1 and 50: " + size);
        }

        Pageable pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("createdAt")));
        Page<PostEntity> entityPage = postRepository.findAll(pageable);

        List<PostResponse> items = entityPage.getContent().stream()
                .map(PostResponse::fromEntity)
                .toList();

        return new PagedPostResponse(
                items,
                page,
                size,
                entityPage.getTotalSize(),
                entityPage.getTotalPages()
        );
    }

    /**
     * Validates, normalizes, and records a new post with a UTC timestamp.
     *
     * @param command the user input request payload
     * @return the persisted {@link PostResponse} containing generated ID and timestamp
     * @throws IllegalArgumentException if command is null or contains blank required fields
     */
    public PostResponse createPost(CreatePostRequest command) {
        if (command == null) {
            throw new IllegalArgumentException("CreatePostRequest command must not be null");
        }

        String name = command.name() != null ? command.name().strip() : "";
        String title = command.title() != null ? command.title().strip() : "";
        String message = command.message() != null ? command.message().strip() : "";

        if (name.isEmpty() || title.isEmpty() || message.isEmpty()) {
            throw new IllegalArgumentException("Name, title, and message must not be blank or whitespace-only");
        }

        String normalizedEmail = (command.email() != null && !command.email().isBlank())
                ? command.email().strip()
                : null;

        LocalDateTime now = LocalDateTime.now(clock);

        PostEntity entityToSave = new PostEntity(
                null,
                name,
                normalizedEmail,
                title,
                message,
                now
        );

        PostEntity savedEntity = postRepository.save(entityToSave);
        return PostResponse.fromEntity(savedEntity);
    }
}
