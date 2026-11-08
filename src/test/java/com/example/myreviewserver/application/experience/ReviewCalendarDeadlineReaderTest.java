package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ReviewCalendarDeadlineReaderTest {

	private final ReviewCalendarDeadlineReader reader = new ReviewCalendarDeadlineReader();

	@Test
	void readsTheDayAboveAPinkPillOnASundayStartCalendar() throws IOException {
		assertThat(reader.readDayOfMonth(calendar(0, 3, 0))).hasValue(22);
	}

	@Test
	void readsTheDayWhenTheMonthStartsOnWednesday() throws IOException {
		assertThat(reader.readDayOfMonth(calendar(3, 1, 0))).hasValue(5);
	}

	@Test
	void ignoresAPinkChipThatOpensAPeriodBar() throws IOException {
		assertThat(reader.readDayOfMonth(calendarWithPeriodBar())).isEmpty();
	}

	/**
	 * @param dayOneColumn column of the 1st, 0 = Sunday
	 * @param pillWeek week row that holds the pill
	 * @param pillColumn column of the pill
	 */
	private static byte[] calendar(int dayOneColumn, int pillWeek, int pillColumn) throws IOException {
		BufferedImage image = new BufferedImage(420, 320, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 420, 320);
		graphics.setColor(Color.BLACK);
		int originX = 40;
		int originY = 60;
		int pitchX = 36;
		int pitchY = 32;
		for (int week = 0; week < 5; week++) {
			for (int column = 0; column < 7; column++) {
				int offset = week * 7 + column - dayOneColumn;
				if (offset < 0 || offset > 29) {
					continue;
				}
				graphics.fillRect(originX + column * pitchX, originY + week * pitchY, 10, 6);
			}
		}
		graphics.setColor(new Color(230, 72, 112));
		graphics.fillRect(
			originX + pillColumn * pitchX - 2,
			originY + pillWeek * pitchY + 12,
			26,
			12
		);
		graphics.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}

	private static byte[] calendarWithPeriodBar() throws IOException {
		BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(calendar(0, 3, 0)));
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(170, 170, 170));
		graphics.fillRect(80, 168, 140, 8);
		graphics.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}
}
