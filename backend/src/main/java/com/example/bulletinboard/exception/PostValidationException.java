package com.example.bulletinboard.exception;

/**
 * Thrown when business validation on a post command fails.
 */
public class PostValidationException extends BulletinBoardException {

    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PostValidationException(String message) {
        super(message);
    }
}
