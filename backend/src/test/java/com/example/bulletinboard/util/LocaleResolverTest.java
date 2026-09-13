package com.example.bulletinboard.util;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link LocaleResolver} verifying HTTP Accept-Language negotiation contracts.
 */
@DisplayName("Unit test suite for LocaleResolver")
class LocaleResolverTest {

    private LocaleResolver localeResolver;

    @BeforeEach
    void setUp() {
        localeResolver = new HttpLocaleResolver();
    }

    @Test
    @DisplayName("Should enforce non-null request precondition")
    void shouldRejectNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> localeResolver.resolveLocale(null),
                "Should reject null request with IllegalArgumentException");
    }

    @Test
    @DisplayName("Should return default English when request headers collection is null")
    void shouldReturnDefaultWhenHeadersNull() {
        HttpRequest<?> request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(null);

        Locale resolved = localeResolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolved, "Null headers collection must resolve to default Locale.ENGLISH");
    }

    @Test
    @DisplayName("Should return default English when Accept-Language header is absent")
    void shouldReturnDefaultWhenHeaderAbsent() {
        HttpRequest<?> request = HttpRequest.GET("/api/posts");
        Locale resolved = localeResolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolved, "Absent header must resolve to default Locale.ENGLISH");
    }

    @Test
    @DisplayName("Should return default English when Accept-Language header is empty")
    void shouldReturnDefaultWhenHeaderEmpty() {
        HttpRequest<?> request = HttpRequest.GET("/api/posts").header(HttpHeaders.ACCEPT_LANGUAGE, "");
        Locale resolved = localeResolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolved, "Empty header must resolve to default Locale.ENGLISH");
    }

    @Test
    @DisplayName("Should return default English when Accept-Language header contains only whitespace")
    void shouldReturnDefaultWhenHeaderWhitespaceOnly() {
        HttpRequest<?> request = mock(HttpRequest.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn("   ");

        Locale resolved = localeResolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolved, "Whitespace-only header must resolve to default Locale.ENGLISH");
    }

    @ParameterizedTest(name = "Header: ''{0}'' -> Expected Locale: {1}")
    @CsvSource({
            "ja, ja",
            "JA, ja",
            "ja-JP, ja",
            "ja-JP;q=0.9, ja",
            "en, en",
            "EN, en",
            "en-US, en",
            "en-GB, en",
            "'en-US,en;q=0.9,ja;q=0.8', en",
            "'ja-JP,ja;q=0.9,en;q=0.8', ja",
            "'en;q=0.7,ja;q=0.9', ja",
            "'ja;q=0.7,en;q=0.9', en",
            "'ja;q=0, en;q=0.8', en",
            "'ja;q=0', en",
            "'ja, en', ja",
            "'en, ja', en",
            "'ja;q=0.8, en;q=0.8', ja",
            "'en;q=0.8, ja;q=0.8', en",
            "jav, en",
            "jam, en",
            "'ja;q=NaN, en;q=1.0', en",
            "',, ja;q=0.8, ;q=0.5,', ja",
            "'fr-FR,de;q=0.9,ja;q=0.8', ja",
            "'fr-FR,de;q=0.9,en;q=0.8', en",
            "'fr-FR,de;q=0.9', en",
            "'*', en",
            "'*;q=0.8', en",
            "unsupported-language, en"
    })
    @DisplayName("Should negotiate client locale based on Accept-Language quality weights and fallback")
    void shouldNegotiateLocaleFromAcceptLanguage(String headerValue, String expectedLanguageTag) {
        HttpRequest<?> request = HttpRequest.GET("/api/posts").header(HttpHeaders.ACCEPT_LANGUAGE, headerValue);
        Locale resolved = localeResolver.resolveLocale(request);
        assertNotNull(resolved, "Resolved locale must never be null");
        Locale expectedLocale = "ja".equalsIgnoreCase(expectedLanguageTag) ? Locale.JAPANESE : Locale.ENGLISH;
        assertEquals(expectedLocale, resolved,
                () -> "Failed negotiating exact locale from header: " + headerValue);
    }
}
