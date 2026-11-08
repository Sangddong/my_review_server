package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.shared.DomainException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 * Shrinks a screenshot so the long edge is at most {@link #MAX_LONG_EDGE} pixels
 * before it is sent to a vision model.
 */
@Component
public class ScreenshotImageResizer {

	static final int MAX_LONG_EDGE = 1280;

	public ScreenshotImage resize(byte[] original) {
		if (original == null || original.length == 0) {
			throw new DomainException("image is required");
		}
		BufferedImage source = read(original);
		int width = source.getWidth();
		int height = source.getHeight();
		String mediaType = mediaTypeOf(original);
		if (Math.max(width, height) <= MAX_LONG_EDGE && mediaType != null) {
			return new ScreenshotImage(original, mediaType);
		}

		int targetWidth;
		int targetHeight;
		if (Math.max(width, height) <= MAX_LONG_EDGE) {
			targetWidth = width;
			targetHeight = height;
		}
		else if (width >= height) {
			targetWidth = MAX_LONG_EDGE;
			targetHeight = Math.max(1, (int) Math.round(height * (MAX_LONG_EDGE / (double) width)));
		}
		else {
			targetHeight = MAX_LONG_EDGE;
			targetWidth = Math.max(1, (int) Math.round(width * (MAX_LONG_EDGE / (double) height)));
		}

		BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = scaled.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, targetWidth, targetHeight);
			graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		}
		finally {
			graphics.dispose();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			if (!ImageIO.write(scaled, "png", out)) {
				throw new DomainException("unsupported image");
			}
		}
		catch (IOException ex) {
			throw new DomainException("unsupported image");
		}
		return new ScreenshotImage(out.toByteArray(), "image/png");
	}

	private static BufferedImage read(byte[] bytes) {
		try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
			BufferedImage image = ImageIO.read(in);
			if (image == null) {
				throw new DomainException("unsupported image");
			}
			return image;
		}
		catch (IOException ex) {
			throw new DomainException("unsupported image");
		}
	}

	/**
	 * @return a media type the vision API accepts, or null when the bytes must be re-encoded
	 */
	private static String mediaTypeOf(byte[] bytes) {
		if (bytes.length >= 8
			&& (bytes[0] & 0xFF) == 0x89
			&& bytes[1] == 0x50
			&& bytes[2] == 0x4E
			&& bytes[3] == 0x47) {
			return "image/png";
		}
		if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
			return "image/jpeg";
		}
		if (bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
			return "image/gif";
		}
		return null;
	}
}
