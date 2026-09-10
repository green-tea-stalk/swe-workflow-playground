package com.example.bulletinboard;

import io.micronaut.runtime.Micronaut;

/**
 * Entry point for the Bulletin Board Micronaut application.
 */
public class Application {

    /**
     * Bootstraps and starts the Micronaut application context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
