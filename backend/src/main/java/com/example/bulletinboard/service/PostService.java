package com.example.bulletinboard.service;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.CreateReplyRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.dto.ReplyResponse;
import com.example.bulletinboard.entity.PostEntity;
import com.example.bulletinboard.entity.ReplyEntity;
import com.example.bulletinboard.exception.PostNotFoundException;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.ReplyRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Domain service coordinating bulletin board post creation, reply creation, business validation, and paginated feed retrieval.
 */
@Singleton
public class PostService {

    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final Clock clock;

    /**
     * Constructs a new PostService instance.
     *
     * @param postRepository  the underlying post database repository
     * @param replyRepository the underlying reply database repository
     */
    public PostService(PostRepository postRepository, ReplyRepository replyRepository) {
        this(postRepository, replyRepository, Clock.systemUTC());
    }

    /**
     * Package-private constructor enabling deterministic time injection during tests.
     *
     * @param postRepository  the underlying post database repository
     * @param replyRepository the underlying reply database repository
     * @param clock           the clock providing current UTC timestamps
     */
    PostService(PostRepository postRepository, ReplyRepository replyRepository, Clock clock) {
        this.postRepository = Objects.requireNonNull(postRepository, "postRepository must not be null");
        this.replyRepository = Objects.requireNonNull(replyRepository, "replyRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Retrieves a paginated list of posts in reverse chronological order (newest first),
     * embedding associated replies batch-fetched in ascending chronological order.
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

        List<PostEntity> posts = entityPage.getContent();
        if (posts.isEmpty()) {
            return new PagedPostResponse(List.of(), page, size, entityPage.getTotalSize(), entityPage.getTotalPages());
        }

        List<Long> postIds = posts.stream().map(PostEntity::id).toList();
        List<ReplyEntity> replies = replyRepository.findByPostIdInOrderByCreatedAtAsc(postIds);

        // Group replies by parent post ID; defensive null fallback and Collectors.toList() downstream retains encounter
        // order (chronological ASC)
        Map<Long, List<ReplyResponse>> repliesByPostId = (replies != null ? replies : List.<ReplyEntity>of())
                .stream()
                        .collect(Collectors.groupingBy(
                                ReplyEntity::postId,
                                Collectors.mapping(ReplyResponse::fromEntity, Collectors.toList())));

        List<PostResponse> items = posts.stream()
                .map(post -> PostResponse.fromEntity(post, repliesByPostId.getOrDefault(post.id(), List.of())))
                .toList();

        return new PagedPostResponse(items, page, size, entityPage.getTotalSize(), entityPage.getTotalPages());
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

        PostEntity entityToSave = new PostEntity(null, name, normalizedEmail, title, message, now);

        PostEntity savedEntity = postRepository.save(entityToSave);
        return PostResponse.fromEntity(savedEntity);
    }

    /**
     * Validates, normalizes, and records a new reply associated with an existing post.
     *
     * @param postId  the parent post ID (must be non-null and strictly positive)
     * @param command the reply creation request payload
     * @return the persisted {@link ReplyResponse} containing generated ID and timestamp
     * @throws IllegalArgumentException if postId is non-positive or command contains blank required fields
     * @throws PostNotFoundException    if the target parent post does not exist
     */
    public ReplyResponse createReply(Long postId, CreateReplyRequest command) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("Post ID must be non-null and strictly positive: " + postId);
        }
        if (command == null) {
            throw new IllegalArgumentException("CreateReplyRequest command must not be null");
        }

        String name = command.name() != null ? command.name().strip() : "";
        String message = command.message() != null ? command.message().strip() : "";

        if (name.isEmpty() || message.isEmpty()) {
            throw new IllegalArgumentException("Name and message must not be blank or whitespace-only");
        }

        // Enforce domain PostNotFoundException before persistence rather than relying on database foreign key
        // constraint
        postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

        String normalizedEmail = (command.email() != null && !command.email().isBlank())
                ? command.email().strip()
                : null;

        LocalDateTime now = LocalDateTime.now(clock);

        ReplyEntity entityToSave = new ReplyEntity(null, postId, name, normalizedEmail, message, now);
        ReplyEntity savedEntity = replyRepository.save(entityToSave);

        return ReplyResponse.fromEntity(savedEntity);
    }
}
