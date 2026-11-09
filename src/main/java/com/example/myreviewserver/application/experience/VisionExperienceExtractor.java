package com.example.myreviewserver.application.experience;

import java.util.List;

/**
 * Reads experience form fields from one screenshot.
 * Implementations must not store the image.
 * {@code userPlatformNames} is the signed-in user's saved platform tag names.
 * The model compares labels it reads on the page with these names. Ids are not sent.
 */
public interface VisionExperienceExtractor {

	ExtractedExperienceDraft extract(ScreenshotImage image, List<String> userPlatformNames);
}
