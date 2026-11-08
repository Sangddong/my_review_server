package com.example.myreviewserver.application.experience;

/**
 * In-memory screenshot passed to the vision model.
 * Callers must not persist these bytes.
 */
public record ScreenshotImage(byte[] bytes, String mediaType) {
}
