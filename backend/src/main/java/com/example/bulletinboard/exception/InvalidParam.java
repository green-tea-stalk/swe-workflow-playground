package com.example.bulletinboard.exception;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Detailed description of an invalid request parameter conforming to RFC 9457 extensions.
 *
 * @param name   the field or parameter name that violated a validation constraint
 * @param reason a human-readable explanation describing the violation
 */
@Serdeable
public record InvalidParam(
        String name,
        String reason
) {}
