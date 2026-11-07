package com.example.myreviewserver.application.experience;

/**
 * Reads experience form fields from one screenshot.
 * Implementations must not store the image.
 */
public interface VisionExperienceExtractor {

	ExtractedExperienceDraft extract(ScreenshotImage image);
}
