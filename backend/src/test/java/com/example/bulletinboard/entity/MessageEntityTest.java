package com.example.bulletinboard.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("掲示板メッセージエンティティの単体テスト")
class MessageEntityTest {

    @Test
    @DisplayName("全項目を指定してメッセージエンティティを生成できること")
    void createMessageEntityWithAllFields() {
        Long expectedId = 1L;
        String expectedName = "山田 太郎";
        String expectedEmail = "yamada@example.com";
        String expectedTitle = "テスト投稿タイトル";
        String expectedMessage = "これはテストメッセージ本文です。";
        Instant expectedCreatedAt = Instant.parse("2026-09-05T12:00:00Z");

        MessageEntity entity = new MessageEntity(
                expectedId,
                expectedName,
                expectedEmail,
                expectedTitle,
                expectedMessage,
                expectedCreatedAt
        );

        assertAll(
                () -> assertEquals(expectedId, entity.id(), "IDが一致すること"),
                () -> assertEquals(expectedName, entity.name(), "名前が一致すること"),
                () -> assertEquals(expectedEmail, entity.email(), "メールアドレスが一致すること"),
                () -> assertEquals(expectedTitle, entity.title(), "タイトルが一致すること"),
                () -> assertEquals(expectedMessage, entity.message(), "メッセージ本文が一致すること"),
                () -> assertEquals(expectedCreatedAt, entity.createdAt(), "作成日時が一致すること")
        );
    }

    @Test
    @DisplayName("永続化前（IDがnull）のメッセージエンティティを生成できること")
    void createMessageEntityWithNullIdBeforePersistence() {
        String expectedName = "名無しさん";
        String expectedEmail = "anonymous@example.com";
        String expectedTitle = "新規投稿";
        String expectedMessage = "永続化前エンティティのテストです。";
        Instant expectedCreatedAt = Instant.parse("2026-09-05T12:00:00Z");

        MessageEntity entity = new MessageEntity(
                null,
                expectedName,
                expectedEmail,
                expectedTitle,
                expectedMessage,
                expectedCreatedAt
        );

        assertAll(
                () -> assertNull(entity.id(), "永続化前のIDはnullであること"),
                () -> assertEquals(expectedName, entity.name(), "名前が一致すること"),
                () -> assertEquals(expectedEmail, entity.email(), "メールアドレスが一致すること"),
                () -> assertEquals(expectedTitle, entity.title(), "タイトルが一致すること"),
                () -> assertEquals(expectedMessage, entity.message(), "メッセージ本文が一致すること"),
                () -> assertEquals(expectedCreatedAt, entity.createdAt(), "作成日時が一致すること")
        );
    }

    @Test
    @DisplayName("メールアドレスがnullの場合でも正常にメッセージエンティティを生成できること")
    void createMessageEntityWithNullEmail() {
        Long expectedId = 2L;
        String expectedName = "名無しさん";
        String expectedTitle = "匿名タイトル";
        String expectedMessage = "メールなしの投稿です。";
        Instant expectedCreatedAt = Instant.parse("2026-09-05T12:00:00Z");

        MessageEntity entity = new MessageEntity(
                expectedId,
                expectedName,
                null,
                expectedTitle,
                expectedMessage,
                expectedCreatedAt
        );

        assertAll(
                () -> assertEquals(expectedId, entity.id(), "IDが一致すること"),
                () -> assertEquals(expectedName, entity.name(), "名前が一致すること"),
                () -> assertNull(entity.email(), "メールアドレスがnullであること"),
                () -> assertEquals(expectedTitle, entity.title(), "タイトルが一致すること"),
                () -> assertEquals(expectedMessage, entity.message(), "メッセージ本文が一致すること"),
                () -> assertEquals(expectedCreatedAt, entity.createdAt(), "作成日時が一致すること")
        );
    }

    @ParameterizedTest(name = "ID: {0}, 名前: {1} のエンティティ等価性検証")
    @CsvSource({
            "1, アリス, alice@example.com, タイトル1, メッセージ1",
            "2, ボブ, bob@example.com, タイトル2, メッセージ2"
    })
    @DisplayName("同一属性値を持つメッセージエンティティは等価であること")
    void messageEntitiesWithIdenticalAttributesAreEqual(Long id, String name, String email, String title, String message) {
        Instant fixedTime = Instant.parse("2026-09-05T10:00:00Z");
        MessageEntity entity1 = new MessageEntity(id, name, email, title, message, fixedTime);
        MessageEntity entity2 = new MessageEntity(id, name, email, title, message, fixedTime);

        assertAll(
                () -> assertEquals(entity1, entity2, "同一値のインスタンスはequalsでtrueとなること"),
                () -> assertEquals(entity1.hashCode(), entity2.hashCode(), "同一値のインスタンスは同一のhashCodeを持つこと")
        );
    }

    @Test
    @DisplayName("異なる属性値を持つメッセージエンティティは不等価であること")
    void messageEntitiesWithDifferentAttributesAreNotEqual() {
        Instant fixedTime = Instant.parse("2026-09-05T10:00:00Z");
        MessageEntity baseEntity = new MessageEntity(1L, "アリス", "alice@example.com", "タイトル", "本文", fixedTime);
        MessageEntity differentId = new MessageEntity(2L, "アリス", "alice@example.com", "タイトル", "本文", fixedTime);
        MessageEntity differentName = new MessageEntity(1L, "ボブ", "alice@example.com", "タイトル", "本文", fixedTime);

        assertAll(
                () -> assertNotEquals(baseEntity, differentId, "IDが異なる場合は不等価であること"),
                () -> assertNotEquals(baseEntity, differentName, "名前が異なる場合は不等価であること"),
                () -> assertNotEquals(baseEntity, null, "nullと比較した場合は不等価であること"),
                () -> assertNotEquals(baseEntity, new Object(), "異なる型と比較した場合は不等価であること")
        );
    }
}
