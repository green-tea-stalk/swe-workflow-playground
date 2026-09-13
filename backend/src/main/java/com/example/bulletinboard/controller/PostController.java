package com.example.bulletinboard.controller;

import com.example.bulletinboard.dto.CreatePostRequest;
import com.example.bulletinboard.dto.PagedPostResponse;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.service.PostService;
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

import java.net.URI;
import java.util.Objects;

/**
 * REST controller exposing bulletin board endpoints for feed retrieval and post submission.
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
            @QueryValue(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @QueryValue(defaultValue = "50") @Min(value = 1, message = "Size must be between 1 and 50") @Max(value = 50, message = "Size must be between 1 and 50") int size
    ) {
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
}
