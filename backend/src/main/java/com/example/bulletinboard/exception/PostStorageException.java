package com.example.bulletinboard.exception;

/**
 * Thrown when underlying persistence or relational database operations fail.
 */
public class PostStorageException extends BulletinBoardException {

    /**
     * Constructs a new storage exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public PostStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
