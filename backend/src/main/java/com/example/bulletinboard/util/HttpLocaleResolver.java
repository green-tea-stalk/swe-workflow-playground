package com.example.bulletinboard.util;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * HTTP content negotiation implementation of {@link LocaleResolver} that evaluates Accept-Language headers.
 */
@Singleton
public class HttpLocaleResolver implements LocaleResolver {

    private static final double DEFAULT_QUALITY = 1.0;
    private static final int MAX_HEADER_LENGTH = 4096;

    /**
     * {@inheritDoc}
     *
     * <p>Parses the {@code Accept-Language} HTTP header, prioritizing requested languages by quality value
     * (q-factor). Resolves to {@link Locale#JAPANESE} for {@code ja} or {@code ja-*} tags, {@link Locale#ENGLISH}
     * for {@code en} or {@code en-*} tags, and defaults to {@link Locale#ENGLISH} for absent, wildcard, or unsupported
     * language preferences.</p>
     */
    @Override
    public Locale resolveLocale(HttpRequest<?> request) {
        if (request == null) {
            throw new IllegalArgumentException("HttpRequest must not be null");
        }

        HttpHeaders headers = request.getHeaders();
        if (headers == null) {
            return Locale.ENGLISH;
        }

        String acceptLanguage = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
        if (acceptLanguage == null || acceptLanguage.isBlank() || acceptLanguage.length() > MAX_HEADER_LENGTH) {
            return Locale.ENGLISH;
        }

        List<LanguageRange> ranges = parseRanges(acceptLanguage);
        ranges.sort(Comparator.comparingDouble(LanguageRange::quality).reversed()
                .thenComparingInt(LanguageRange::originalIndex));

        for (LanguageRange range : ranges) {
            if (range.quality() <= 0.0) {
                continue;
            }
            String tag = range.tag().toLowerCase(Locale.ROOT);
            if (isJapaneseTag(tag)) {
                return Locale.JAPANESE;
            }
            if (isEnglishTag(tag)) {
                return Locale.ENGLISH;
            }
        }

        return Locale.ENGLISH;
    }

    private static boolean isJapaneseTag(String tag) {
        return tag.equals("ja") || tag.startsWith("ja-") || tag.startsWith("ja_");
    }

    private static boolean isEnglishTag(String tag) {
        return tag.equals("en") || tag.startsWith("en-") || tag.startsWith("en_");
    }

    private List<LanguageRange> parseRanges(String headerValue) {
        String[] tokens = headerValue.split(",");
        List<LanguageRange> ranges = new ArrayList<>(tokens.length);

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) {
                continue;
            }

            String tag = token;
            double quality = DEFAULT_QUALITY;

            int semicolonIndex = token.indexOf(';');
            if (semicolonIndex != -1) {
                tag = token.substring(0, semicolonIndex).trim();
                String params = token.substring(semicolonIndex + 1).trim();
                quality = parseQuality(params);
            }

            if (!tag.isEmpty()) {
                ranges.add(new LanguageRange(tag, quality, i));
            }
        }

        return ranges;
    }

    private double parseQuality(String params) {
        for (String param : params.split(";")) {
            String trimmed = param.trim();
            if (trimmed.startsWith("q=") || trimmed.startsWith("Q=")) {
                try {
                    double parsed = Double.parseDouble(trimmed.substring(2).trim());
                    if (!Double.isFinite(parsed)) {
                        return 0.0;
                    }
                    return Math.max(0.0, Math.min(1.0, parsed));
                } catch (NumberFormatException ignored) {
                    return 0.0;
                }
            }
        }
        return DEFAULT_QUALITY;
    }

    private record LanguageRange(String tag, double quality, int originalIndex) {}
}
