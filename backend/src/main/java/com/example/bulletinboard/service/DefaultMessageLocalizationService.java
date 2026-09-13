package com.example.bulletinboard.service;

import jakarta.inject.Singleton;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Default implementation of {@link MessageLocalizationService} backed by {@link ResourceBundle}.
 * Uses non-fallback bundle resolution to isolate locale resolution from host JVM default locale.
 */
@Singleton
public class DefaultMessageLocalizationService implements MessageLocalizationService {

    private static final String BUNDLE_NAME = "messages";
    private static final ResourceBundle.Control NO_FALLBACK_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    /**
     * {@inheritDoc}
     *
     * <p>Resolves message string without parameter formatting, delegating to {@link #getMessage(String, Locale, Object...)}.</p>
     */
    @Override
    public String getMessage(String code, Locale locale) {
        return getMessage(code, locale, (Object[]) null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves message against target locale, falling back to English root bundle.
     * When unresolved, returns the unformatted code identifier directly without argument parsing.</p>
     */
    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        validateInputs(code, locale);
        String resolved = resolveFromBundles(code, locale, args);
        return resolved != null ? resolved : code;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves message against target locale, falling back to English root bundle.
     * When unresolved, formats and returns the provided default message.</p>
     */
    @Override
    public String getMessageOrDefault(String code, Locale locale, String defaultMessage, Object... args) {
        validateInputs(code, locale);
        String resolved = resolveFromBundles(code, locale, args);
        return resolved != null ? resolved : formatMessage(defaultMessage, locale, args);
    }

    private String resolveFromBundles(String code, Locale locale, Object[] args) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, NO_FALLBACK_CONTROL);
            if (bundle.containsKey(code)) {
                return formatMessage(bundle.getString(code), locale, args);
            }
        } catch (MissingResourceException ignored) {
            // Target bundle not present on classpath
        }

        try {
            ResourceBundle rootBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT, NO_FALLBACK_CONTROL);
            if (rootBundle.containsKey(code)) {
                return formatMessage(rootBundle.getString(code), Locale.ENGLISH, args);
            }
        } catch (MissingResourceException ignored) {
            // Root bundle not present on classpath
        }

        return null;
    }

    private String formatMessage(String pattern, Locale locale, Object[] args) {
        if (pattern == null || args == null || args.length == 0) {
            return pattern;
        }
        try {
            MessageFormat formatter = new MessageFormat(pattern, locale);
            return formatter.format(args);
        } catch (IllegalArgumentException e) {
            return pattern;
        }
    }

    private void validateInputs(String code, Locale locale) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Message code must not be null or blank");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale must not be null");
        }
    }
}
