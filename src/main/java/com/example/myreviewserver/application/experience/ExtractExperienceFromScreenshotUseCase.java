package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
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
	private final PlatformRepository platformRepository;

	public ExtractExperienceFromScreenshotUseCase(
		ScreenshotImageResizer resizer,
		VisionExperienceExtractor extractor,
		PlatformRepository platformRepository
	) {
		this.resizer = resizer;
		this.extractor = extractor;
		this.platformRepository = platformRepository;
	}

	public ExtractedExperienceDraft extract(Long userId, byte[] imageBytes) {
		ScreenshotImage image = resizer.resize(imageBytes);
		List<Platform> platforms = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
		List<String> platformNames = platforms.stream().map(Platform::getName).toList();
		ExtractedExperienceDraft draft = extractor.extract(image, platformNames);
		if (draft == null) {
			throw new DomainException("screenshot extraction failed");
		}
		return new ExtractedExperienceDraft(
			draft.name(),
			draft.experienceType(),
			draft.reviewDeadline(),
			draft.platformNames(),
			draft.detailLink(),
			UserPlatformMatcher.matchIds(platforms, draft.platformNames())
		);
	}
}
