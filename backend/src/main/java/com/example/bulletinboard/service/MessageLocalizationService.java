package com.example.bulletinboard.service;

import java.util.Locale;

/**
 * Domain service contract for resolving localized message strings from resource bundles.
 */
public interface MessageLocalizationService {

    /**
     * Resolves a localized message for the specified code and locale.
     *
     * @param code   the message code identifier (must not be null or blank)
     * @param locale the target locale (must not be null)
     * @return the localized message string, or code if not found
     * @throws IllegalArgumentException if code is null/blank or locale is null
     */
    String getMessage(String code, Locale locale);

    /**
     * Resolves a localized message for the specified code and locale with parameter interpolation.
     *
     * @param code   the message code identifier (must not be null or blank)
     * @param locale the target locale (must not be null)
     * @param args   optional arguments for message formatting
     * @return the formatted localized message string, or code if not found
     * @throws IllegalArgumentException if code is null/blank or locale is null
     */
    String getMessage(String code, Locale locale, Object... args);

    /**
     * Resolves a localized message for the specified code and locale, returning a default message if unresolved.
     *
     * @param code           the message code identifier (must not be null or blank)
     * @param locale         the target locale (must not be null)
     * @param defaultMessage fallback message string to return if code is missing across bundles
     * @param args           optional arguments for message formatting
     * @return the localized message string, or defaultMessage if not found
     * @throws IllegalArgumentException if code is null/blank or locale is null
     */
    String getMessageOrDefault(String code, Locale locale, String defaultMessage, Object... args);
}
