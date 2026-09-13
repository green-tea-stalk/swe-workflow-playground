package com.example.bulletinboard.util;

import io.micronaut.http.HttpRequest;

import java.util.Locale;

/**
 * Adapter interface for resolving client {@link Locale} from incoming HTTP requests based on content negotiation.
 */
public interface LocaleResolver {

    /**
     * Resolves the target Locale from the given HTTP request.
     *
     * @param request the incoming HTTP request (must not be null)
     * @return the resolved non-null Locale (defaults to {@link Locale#ENGLISH} if unspecified or unsupported)
     * @throws IllegalArgumentException if request is null
     */
    Locale resolveLocale(HttpRequest<?> request);
}
