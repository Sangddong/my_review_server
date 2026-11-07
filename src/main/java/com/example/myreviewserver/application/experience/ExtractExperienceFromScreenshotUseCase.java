package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
import java.util.OptionalInt;
import org.springframework.stereotype.Service;

/**
 * Turns one uploaded screenshot into form fields.
 * The image is resized, sent to the vision model, and then dropped.
 * It is not written to disk or the database.
 *
 * @Service: 서비스 빈.
 */
@Service
public class ExtractExperienceFromScreenshotUseCase {

	private final ScreenshotImageResizer resizer;
	private final VisionExperienceExtractor extractor;
	private final ReviewCalendarDeadlineReader deadlineReader;

	public ExtractExperienceFromScreenshotUseCase(
		ScreenshotImageResizer resizer,
		VisionExperienceExtractor extractor,
		ReviewCalendarDeadlineReader deadlineReader
	) {
		this.resizer = resizer;
		this.extractor = extractor;
		this.deadlineReader = deadlineReader;
	}

	public ExtractedExperienceDraft extract(byte[] imageBytes) {
		ScreenshotImage image = resizer.resize(imageBytes);
		ExtractedExperienceDraft draft = extractor.extract(image);
		if (draft == null) {
			throw new DomainException("screenshot extraction failed");
		}
		return withCalendarDay(draft, image.bytes());
	}

	/**
	 * The model reads the calendar month, but tiny day digits are unreliable.
	 * When a pink deadline pill sits on a month grid, that pill's day replaces
	 * the model's day and the model's year and month stay.
	 */
	private ExtractedExperienceDraft withCalendarDay(ExtractedExperienceDraft draft, byte[] imageBytes) {
		LocalDate deadline = draft.reviewDeadline();
		if (deadline == null) {
			return draft;
		}
		OptionalInt day = deadlineReader.readDayOfMonth(imageBytes);
		if (day.isEmpty() || day.getAsInt() > deadline.lengthOfMonth()) {
			return draft;
		}
		return new ExtractedExperienceDraft(
			draft.name(),
			draft.experienceType(),
			deadline.withDayOfMonth(day.getAsInt()),
			draft.platformNames(),
			draft.detailLink()
		);
	}
}
