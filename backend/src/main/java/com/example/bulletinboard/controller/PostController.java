package com.example.bulletinboard.controller;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.CreateReplyRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.dto.ReplyResponse;
import com.example.bulletinboard.service.PostService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.Objects;

/**
 * REST controller exposing bulletin board endpoints for feed retrieval, post submission, and reply creation.
 */
@Controller("/api/posts")
@Validated
public class PostController {

    private final PostService postService;

    /**
     * Constructs a new PostController instance.
     *
     * @param postService the domain post service
     */
    public PostController(PostService postService) {
        this.postService = Objects.requireNonNull(postService, "postService must not be null");
    }

    /**
     * Retrieves a paginated feed of posts in reverse-chronological order.
     *
     * @param page zero-based page index (minimum: 0, default: 0)
     * @param size page size limit (minimum: 1, maximum: 50, default: 50)
     * @return HTTP 200 OK containing {@link PagedPostResponse} with guaranteed empty list on zero results
     */
    @Get(produces = MediaType.APPLICATION_JSON)
    public HttpResponse<PagedPostResponse> listPosts(
            @QueryValue(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0")
                    int page,
            @QueryValue(defaultValue = "50")
                    @Min(value = 1, message = "Size must be between 1 and 50")
                    @Max(value = 50, message = "Size must be between 1 and 50")
                    int size) {
        PagedPostResponse response = postService.getPagedPosts(page, size);
        return HttpResponse.ok(response);
    }

    /**
     * Submits and records a new post with validation and UTC timestamp assignment.
     *
     * @param request the post creation payload
     * @return HTTP 201 Created containing {@link PostResponse} and Location header
     */
    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<PostResponse> createPost(@Body @Valid @NotNull CreatePostRequest request) {
        PostResponse response = postService.createPost(request);
        return HttpResponse.created(response, URI.create("/api/posts/" + response.id()));
    }

    /**
     * Submits and records a new reply associated with an existing post.
     *
     * <p>Returns HTTP 201 Created on success, HTTP 400 Bad Request on validation failure,
     * or HTTP 404 Not Found if the parent post does not exist.</p>
     *
     * @param postId  the parent post identifier (must be &gt;= 1)
     * @param request the reply submission payload
     * @return HTTP 201 Created containing {@link ReplyResponse} and Location header
     */
    @Post(uri = "/{postId}/replies", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<ReplyResponse> createReply(
            @PathVariable("postId")
                    @NotNull(message = "Post ID must not be null")
                    @Min(value = 1, message = "Post ID must be greater than or equal to 1")
                    Long postId,
            @Body @Valid @NotNull CreateReplyRequest request) {
        ReplyResponse response = postService.createReply(postId, request);
        return HttpResponse.created(response, URI.create("/api/posts/" + postId + "/replies/" + response.id()));
    }
}
