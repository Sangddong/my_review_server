package com.example.myreviewserver.application.experience;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 * Reads the day number of a pink or red deadline pill on a month calendar.
 * Vision models miss digits that are only a few pixels tall, but the pill's
 * column and week are visible as color and as rows of date text.
 *
 * @Component: 서비스에서 주입되는 유틸 빈.
 */
@Component
public class ReviewCalendarDeadlineReader {

	public OptionalInt readDayOfMonth(byte[] imageBytes) {
		if (imageBytes == null || imageBytes.length == 0) {
			return OptionalInt.empty();
		}
		BufferedImage image;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		}
		catch (IOException ex) {
			return OptionalInt.empty();
		}
		if (image == null) {
			return OptionalInt.empty();
		}
		List<Integer> days = new ArrayList<>();
		for (Pill pill : pills(image)) {
			int day = dayOf(image, pill);
			if (day >= 1 && day <= 31 && !days.contains(day)) {
				days.add(day);
			}
		}
		if (days.size() != 1) {
			return OptionalInt.empty();
		}
		return OptionalInt.of(days.get(0));
	}

	private static int dayOf(BufferedImage image, Pill pill) {
		int x0 = Math.max(0, pill.x - 20);
		int x1 = Math.min(image.getWidth(), pill.x + pill.width + 320);
		List<Band> chain = dateChain(image, x0, x1, pill);
		if (chain.size() < 3) {
			return -1;
		}
		int week = -1;
		int bestDelta = Integer.MAX_VALUE;
		for (int index = 0; index < chain.size(); index++) {
			int delta = pill.centerY() - chain.get(index).center();
			if (delta >= 4 && delta <= 26 && delta < bestDelta) {
				bestDelta = delta;
				week = index;
			}
		}
		if (week < 0) {
			return -1;
		}
		List<Cluster> columns = fullestWeek(image, x0, x1, chain);
		if (columns.size() != 7) {
			return -1;
		}
		int[] columnX = new int[7];
		for (int index = 0; index < 7; index++) {
			columnX[index] = columns.get(index).center();
		}
		int pitch = columnX[6] - columnX[0];
		if (pitch < 70) {
			return -1;
		}
		int pillColumn = nearest(columnX, pill.centerX());
		if (Math.abs(columnX[pillColumn] - pill.centerX()) > (columnX[1] - columnX[0]) * 0.6) {
			return -1;
		}
		int dayOneColumn = dayOneColumn(image, x0, x1, chain.get(0), columnX);
		if (dayOneColumn < 0) {
			return -1;
		}
		return 1 + week * 7 + (pillColumn - dayOneColumn);
	}

	private static List<Band> dateChain(BufferedImage image, int x0, int x1, Pill pill) {
		List<Band> bands = bands(image, x0, x1);
		List<Band> best = List.of();
		List<Band> current = new ArrayList<>();
		for (Band band : bands) {
			if (!current.isEmpty()) {
				int gap = band.center() - current.get(current.size() - 1).center();
				if (gap < 24 || gap > 42) {
					best = longer(best, current, pill);
					current = new ArrayList<>();
				}
			}
			current.add(band);
		}
		return longer(best, current, pill);
	}

	private static List<Band> longer(List<Band> best, List<Band> current, Pill pill) {
		if (current.size() < 3) {
			return best;
		}
		if (current.size() > best.size()) {
			return List.copyOf(current);
		}
		if (current.size() == best.size() && distance(current, pill) < distance(best, pill)) {
			return List.copyOf(current);
		}
		return best;
	}

	private static int distance(List<Band> chain, Pill pill) {
		int best = Integer.MAX_VALUE;
		for (Band band : chain) {
			best = Math.min(best, Math.abs(pill.centerY() - band.center()));
		}
		return best;
	}

	private static List<Cluster> fullestWeek(BufferedImage image, int x0, int x1, List<Band> chain) {
		List<Cluster> fullest = List.of();
		for (Band band : chain) {
			List<Cluster> clusters = clusters(image, x0, x1, band.top, band.bottom);
			if (clusters.size() > fullest.size()) {
				fullest = clusters;
			}
		}
		return fullest;
	}

	private static int dayOneColumn(BufferedImage image, int x0, int x1, Band firstWeek, int[] columnX) {
		int dayOne = -1;
		for (Cluster cluster : clusters(image, x0, x1, firstWeek.top, firstWeek.bottom)) {
			int column = nearest(columnX, cluster.center());
			if (Math.abs(columnX[column] - cluster.center()) > (columnX[1] - columnX[0]) * 0.45) {
				continue;
			}
			if (dayOne < 0 || column < dayOne) {
				dayOne = column;
			}
		}
		return dayOne;
	}

	private static List<Band> bands(BufferedImage image, int x0, int x1) {
		int height = image.getHeight();
		int[] darkCount = new int[height];
		for (int y = 0; y < height; y++) {
			int count = 0;
			for (int x = x0; x < x1; x++) {
				if (dark(image.getRGB(x, y))) {
					count++;
				}
			}
			darkCount[y] = count;
		}
		List<Band> bands = new ArrayList<>();
		int y = 0;
		while (y < height) {
			if (darkCount[y] < 6) {
				y++;
				continue;
			}
			int top = y;
			while (y < height && darkCount[y] >= 6) {
				y++;
			}
			int bottom = y - 1;
			if (bottom - top <= 14) {
				bands.add(new Band(top, bottom));
			}
		}
		return bands;
	}

	private static List<Cluster> clusters(BufferedImage image, int x0, int x1, int top, int bottom) {
		List<Cluster> clusters = new ArrayList<>();
		int x = x0;
		while (x < x1) {
			if (!columnHasDark(image, x, top, bottom)) {
				x++;
				continue;
			}
			int start = x;
			while (x < x1 && columnHasDark(image, x, top, bottom)) {
				x++;
			}
			clusters.add(new Cluster(start, x - 1));
		}
		List<Cluster> merged = new ArrayList<>();
		for (Cluster cluster : clusters) {
			if (merged.isEmpty()) {
				merged.add(cluster);
				continue;
			}
			Cluster previous = merged.get(merged.size() - 1);
			if (cluster.start - previous.end <= 12) {
				merged.set(merged.size() - 1, new Cluster(previous.start, cluster.end));
			}
			else {
				merged.add(cluster);
			}
		}
		return merged;
	}

	private static boolean columnHasDark(BufferedImage image, int x, int top, int bottom) {
		for (int y = top; y <= bottom; y++) {
			if (dark(image.getRGB(x, y))) {
				return true;
			}
		}
		return false;
	}

	private static List<Pill> pills(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		boolean[][] seen = new boolean[height][width];
		List<Pill> pills = new ArrayList<>();
		for (int y = 40; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (seen[y][x] || !pink(image.getRGB(x, y))) {
					continue;
				}
				int[] box = {x, y, x, y, 0};
				flood(image, seen, x, y, box);
				int pillWidth = box[2] - box[0] + 1;
				int pillHeight = box[3] - box[1] + 1;
				if (pillWidth >= 12 && pillWidth <= 90 && pillHeight >= 6 && pillHeight <= 24
					&& pillWidth >= pillHeight * 1.4 && box[4] >= 40) {
					pills.add(new Pill(box[0], box[1], pillWidth, pillHeight));
				}
			}
		}
		return pills;
	}

	private static void flood(BufferedImage image, boolean[][] seen, int startX, int startY, int[] box) {
		int width = image.getWidth();
		int height = image.getHeight();
		ArrayList<Integer> stack = new ArrayList<>();
		stack.add(startY * width + startX);
		seen[startY][startX] = true;
		while (!stack.isEmpty()) {
			int index = stack.remove(stack.size() - 1);
			int x = index % width;
			int y = index / width;
			box[0] = Math.min(box[0], x);
			box[1] = Math.min(box[1], y);
			box[2] = Math.max(box[2], x);
			box[3] = Math.max(box[3], y);
			box[4]++;
			visit(image, seen, stack, x - 1, y, width, height);
			visit(image, seen, stack, x + 1, y, width, height);
			visit(image, seen, stack, x, y - 1, width, height);
			visit(image, seen, stack, x, y + 1, width, height);
		}
	}

	private static void visit(
		BufferedImage image,
		boolean[][] seen,
		List<Integer> stack,
		int x,
		int y,
		int width,
		int height
	) {
		if (x < 0 || y < 0 || x >= width || y >= height || seen[y][x] || !pink(image.getRGB(x, y))) {
			return;
		}
		seen[y][x] = true;
		stack.add(y * width + x);
	}

	private static int nearest(int[] columnX, int x) {
		int best = 0;
		int bestDistance = Math.abs(columnX[0] - x);
		for (int index = 1; index < columnX.length; index++) {
			int distance = Math.abs(columnX[index] - x);
			if (distance < bestDistance) {
				best = index;
				bestDistance = distance;
			}
		}
		return best;
	}

	private static boolean dark(int rgb) {
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = rgb & 0xff;
		return (red + green + blue) / 3 < 130;
	}

	private static boolean pink(int rgb) {
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = rgb & 0xff;
		return red > 190 && green < 150 && blue < 190 && red > green + 70 && red > blue + 30;
	}

	private record Band(int top, int bottom) {

		int center() {
			return (top + bottom) / 2;
		}
	}

	private record Cluster(int start, int end) {

		int center() {
			return (start + end) / 2;
		}
	}

	private record Pill(int x, int y, int width, int height) {

		int centerX() {
			return x + width / 2;
		}

		int centerY() {
			return y + height / 2;
		}
	}
}
