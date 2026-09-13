package com.example.bulletinboard.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit and integration test suite for MessageLocalizationService verifying resource bundle resolution,
 * fallback behavior, argument interpolation, and absence safety.
 */
@MicronautTest(environments = "test")
@DisplayName("MessageLocalizationService specification tests")
class MessageLocalizationServiceTest {

    @Inject
    MessageLocalizationService messageLocalizationService;

    @Test
    @DisplayName("Should inject MessageLocalizationService bean cleanly")
    void serviceShouldBeInjected() {
        assertNotNull(messageLocalizationService, "MessageLocalizationService bean should be present in context");
    }

    @ParameterizedTest(name = "Locale: {0}, Key: {1} -> Expected: {2}")
    @CsvSource({
            "en, error.validation.title, Validation Failed",
            "en, error.validation.detail, Input payload failed validation constraints.",
            "en, error.internal.title, Internal Server Error",
            "en, error.internal.detail, An unexpected error occurred while processing the request.",
            "en, error.invalid_arg.title, Invalid Argument",
            "en, validation.name.required, Name must not be blank",
            "en, validation.name.size, Name must be between 1 and 50 characters",
            "en, validation.email.format, Email must be a well-formed email address",
            "en, validation.email.size, Email must not exceed 254 characters",
            "en, validation.title.required, Title must not be blank",
            "en, validation.title.size, Title must be between 1 and 100 characters",
            "en, validation.message.required, Message must not be blank",
            "en, validation.message.size, Message must be between 1 and 4000 characters",
            "ja, error.validation.title, 入力値検証エラー",
            "ja, error.validation.detail, 入力内容に不備があります。制約条件を確認してください。",
            "ja, error.internal.title, サーバー内部エラー",
            "ja, error.internal.detail, リクエストの処理中に予期せぬエラーが発生しました。",
            "ja, error.invalid_arg.title, 不正な引数",
            "ja, validation.name.required, 名前を入力してください",
            "ja, validation.name.size, 名前は1〜50文字以内で入力してください",
            "ja, validation.email.format, 有効なメールアドレス形式で入力してください",
            "ja, validation.email.size, メールアドレスは254文字以内で入力してください",
            "ja, validation.title.required, タイトルを入力してください",
            "ja, validation.title.size, タイトルは1〜100文字以内で入力してください",
            "ja, validation.message.required, メッセージ本文を入力してください",
            "ja, validation.message.size, 'メッセージ本文は1〜4,000文字以内で入力してください'"
    })
    @DisplayName("Should resolve localized messages for standard property keys in English and Japanese")
    void shouldResolveStandardKeys(String localeTag, String code, String expectedMessage) {
        Locale locale = Locale.forLanguageTag(localeTag);
        String resolved = messageLocalizationService.getMessage(code, locale);
        assertEquals(expectedMessage, resolved, () -> "Failed resolving code " + code + " for locale " + localeTag);
    }

    @Test
    @DisplayName("Should fallback to English message when requested locale is unsupported")
    void shouldFallbackToEnglishForUnsupportedLocale() {
        String resolved = messageLocalizationService.getMessage("error.validation.title", Locale.GERMAN);
        assertEquals("Validation Failed", resolved, "Should fallback to English bundle for unsupported language");
    }

    @Test
    @DisplayName("Should return provided default message when key is missing across all bundles")
    void shouldReturnDefaultMessageWhenKeyIsMissing() {
        String defaultMsg = "Fallback default message";
        String resolved = messageLocalizationService.getMessageOrDefault("non.existent.code", Locale.JAPANESE, defaultMsg);
        assertEquals(defaultMsg, resolved, "Should return provided fallback string when code does not exist");
    }

    @Test
    @DisplayName("Should return code safely when key is missing and no default message is provided")
    void shouldReturnCodeSafelyWhenMissingWithoutDefault() {
        String resolved = messageLocalizationService.getMessage("completely.unknown.key", Locale.ENGLISH);
        assertNotNull(resolved, "Should not return null on missing key");
        assertEquals("completely.unknown.key", resolved, "Should return the code itself when key is unresolved");
    }

    @Test
    @DisplayName("Should interpolate arguments into message when parameters are provided")
    void shouldInterpolateArgumentsIntoMessage() {
        String resolved = messageLocalizationService.getMessageOrDefault(
                "nonexistent.code", Locale.ENGLISH, "Pattern with {0} and {1}", "Alpha", 42);
        assertEquals("Pattern with Alpha and 42", resolved, "Should format placeholder arguments");
    }

    @Test
    @DisplayName("Should return unresolved code untouched when missing code contains pattern tokens")
    void shouldReturnCodeSafelyWhenMissingCodeContainsPatternCharacters() {
        String codeWithBraces = "validation.{field}.invalid";
        String resolved = messageLocalizationService.getMessage(codeWithBraces, Locale.ENGLISH, "paramVal");
        assertEquals(codeWithBraces, resolved, "Unresolved code should not be parsed as format pattern");
    }

    @Test
    @DisplayName("Should safely handle null default message when arguments are provided")
    void shouldSafelyHandleNullDefaultMessageWithArguments() {
        String resolved = messageLocalizationService.getMessageOrDefault("missing.code", Locale.ENGLISH, null, "paramVal");
        assertNull(resolved, "Should safely return null without throwing NullPointerException");
    }

    @Test
    @DisplayName("Should gracefully handle malformed pattern in fallback without throwing unhandled exceptions")
    void shouldGracefullyHandleMalformedPatternInFallback() {
        String malformedPattern = "Invalid pattern with unclosed {bracket";
        String resolved = messageLocalizationService.getMessageOrDefault("missing.code", Locale.ENGLISH, malformedPattern, "val");
        assertEquals(malformedPattern, resolved, "Should fall back to unformatted pattern on format error");
    }

    @Test
    @DisplayName("Should reject null, empty, or blank code with IllegalArgumentException")
    void shouldEnforcePreconditionsOnCode() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessage(null, Locale.ENGLISH),
                        "Should reject null code in getMessage"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessage("", Locale.ENGLISH),
                        "Should reject empty code in getMessage"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessage("   ", Locale.ENGLISH),
                        "Should reject whitespace-only code in getMessage"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessage("error.validation.title", null),
                        "Should reject null locale in getMessage"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessageOrDefault(null, Locale.ENGLISH, "default"),
                        "Should reject null code in getMessageOrDefault"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessageOrDefault("   ", Locale.ENGLISH, "default"),
                        "Should reject blank code in getMessageOrDefault"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> messageLocalizationService.getMessageOrDefault("code", null, "default"),
                        "Should reject null locale in getMessageOrDefault")
        );
    }
}
