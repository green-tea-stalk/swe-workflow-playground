package com.example.bulletinboard.exception;

/**
 * Abstract root unchecked exception for domain and infrastructure failures within the bulletin board application.
 */
public abstract class BulletinBoardException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    protected BulletinBoardException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    protected BulletinBoardException(String message, Throwable cause) {
        super(message, cause);
    }
}
