package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;

/**
 * Turns one uploaded screenshot into form fields.
 * The vision model chooses the values, including the review deadline.
 * This service only resizes the image, asks the model, and returns the draft.
 * It does not write the experience or the image.
 *
 * @Service: 서비스 빈.
 */
@Service
public class ExtractExperienceFromScreenshotUseCase {

	private final ScreenshotImageResizer resizer;
	private final VisionExperienceExtractor extractor;

	public ExtractExperienceFromScreenshotUseCase(
		ScreenshotImageResizer resizer,
		VisionExperienceExtractor extractor
	) {
		this.resizer = resizer;
		this.extractor = extractor;
	}

	public ExtractedExperienceDraft extract(byte[] imageBytes) {
		ScreenshotImage image = resizer.resize(imageBytes);
		ExtractedExperienceDraft draft = extractor.extract(image);
		if (draft == null) {
			throw new DomainException("screenshot extraction failed");
		}
		return draft;
	}
}
