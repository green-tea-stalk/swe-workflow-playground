package com.example.bulletinboard;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the Micronaut application context starts successfully.
 */
@MicronautTest(environments = "test")
class ApplicationTest {

    @Inject
    EmbeddedApplication<?> application;

    @Test
    @DisplayName("Micronaut アプリケーションコンテキストが正常に起動すること")
    void testApplicationContextStarts() {
        Assertions.assertTrue(application.isRunning());
    }
}
