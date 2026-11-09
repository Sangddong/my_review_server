package com.example.myreviewserver.application.experience;

import java.util.List;

/**
 * Reads experience form fields from one screenshot.
 * Implementations must not store the image.
 * {@code userPlatformNames} is the signed-in user's saved review platforms, used only as a reference.
 */
public interface VisionExperienceExtractor {

	ExtractedExperienceDraft extract(ScreenshotImage image, List<String> userPlatformNames);
}
