package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.shared.DomainException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ScreenshotImageResizerTest {

	ScreenshotImageResizer resizer = new ScreenshotImageResizer();

	@Test
	void leavesAnImageWithinTheLimitUntouched() throws IOException {
		byte[] original = png(640, 480);

		ScreenshotImage image = resizer.resize(original);

		assertThat(image.bytes()).isEqualTo(original);
		assertThat(image.mediaType()).isEqualTo("image/png");
	}

	@Test
	void shrinksTheLongEdgeTo1280() throws IOException {
		ScreenshotImage wide = resizer.resize(png(2000, 800));
		BufferedImage wideImage = ImageIO.read(new ByteArrayInputStream(wide.bytes()));
		assertThat(wide.mediaType()).isEqualTo("image/png");
		assertThat(wideImage.getWidth()).isEqualTo(1280);
		assertThat(wideImage.getHeight()).isEqualTo(512);

		ScreenshotImage tall = resizer.resize(png(900, 1800));
		BufferedImage tallImage = ImageIO.read(new ByteArrayInputStream(tall.bytes()));
		assertThat(tallImage.getWidth()).isEqualTo(640);
		assertThat(tallImage.getHeight()).isEqualTo(1280);
	}

	@Test
	void rejectsBytesThatAreNotAnImage() {
		assertThatThrownBy(() -> resizer.resize(new byte[] {1, 2, 3, 4}))
			.isInstanceOf(DomainException.class)
			.hasMessage("unsupported image");
	}

	@Test
	void rejectsAnEmptyImage() {
		assertThatThrownBy(() -> resizer.resize(new byte[0]))
			.isInstanceOf(DomainException.class)
			.hasMessage("image is required");
	}

	private static byte[] png(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}
}
