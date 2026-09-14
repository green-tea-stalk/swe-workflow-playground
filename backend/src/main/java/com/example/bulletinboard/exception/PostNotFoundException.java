package com.example.bulletinboard.exception;

/**
 * Thrown when an operation targets a post ID that does not exist in the database.
 */
public class PostNotFoundException extends BulletinBoardException {

    private final Long postId;

    /**
     * Constructs a new PostNotFoundException with the missing post ID.
     *
     * @param postId the post identifier that could not be found
     */
    public PostNotFoundException(Long postId) {
        super("Post with ID " + postId + " not found");
        this.postId = postId;
    }

    /**
     * Retrieves the missing post ID.
     *
     * @return the post identifier
     */
    public Long getPostId() {
        return postId;
    }
}
