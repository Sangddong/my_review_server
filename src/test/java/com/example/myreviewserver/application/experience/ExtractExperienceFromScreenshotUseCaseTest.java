package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.shared.DomainException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ExtractExperienceFromScreenshotUseCaseTest {

	@Test
	void sendsAResizedImageAndReturnsTheDraft() throws IOException {
		RecordingExtractor extractor = new RecordingExtractor();
		ExtractExperienceFromScreenshotUseCase useCase = new ExtractExperienceFromScreenshotUseCase(
			new ScreenshotImageResizer(),
			extractor,
			new ReviewCalendarDeadlineReader()
		);

		ExtractedExperienceDraft draft = useCase.extract(png(2400, 1200));

		assertThat(draft).isEqualTo(extractor.draft);
		BufferedImage sent = ImageIO.read(new ByteArrayInputStream(extractor.image.bytes()));
		assertThat(Math.max(sent.getWidth(), sent.getHeight())).isLessThanOrEqualTo(1280);
		assertThat(sent.getWidth()).isEqualTo(1280);
	}

	@Test
	void doesNotCallTheModelForAnEmptyUpload() {
		RecordingExtractor extractor = new RecordingExtractor();
		ExtractExperienceFromScreenshotUseCase useCase = new ExtractExperienceFromScreenshotUseCase(
			new ScreenshotImageResizer(),
			extractor,
			new ReviewCalendarDeadlineReader()
		);

		assertThatThrownBy(() -> useCase.extract(new byte[0]))
			.isInstanceOf(DomainException.class)
			.hasMessage("image is required");
		assertThat(extractor.image).isNull();
	}

	private static byte[] png(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}

	private static final class RecordingExtractor implements VisionExperienceExtractor {

		ScreenshotImage image;
		ExtractedExperienceDraft draft = new ExtractedExperienceDraft(
			"버질스퀘어 성수",
			ExperienceType.VISIT,
			LocalDate.of(2026, 11, 13),
			List.of("네이버 블로그", "클립"),
			null
		);

		@Override
		public ExtractedExperienceDraft extract(ScreenshotImage image) {
			this.image = image;
			return draft;
		}
	}
}
