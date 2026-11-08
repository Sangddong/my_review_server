package com.example.myreviewserver.adapter.outbound.openai;

import com.example.myreviewserver.application.experience.ExtractedExperienceDraft;
import com.example.myreviewserver.application.experience.ScreenshotImage;
import com.example.myreviewserver.application.experience.VisionExperienceExtractor;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Asks a vision model for experience form fields.
 * The image is sent once and is not stored.
 *
 * @Component: Spring 빈으로 등록되어 VisionExperienceExtractor 구현체로 주입됨.
 */
@Component
public class OpenAiVisionExperienceExtractor implements VisionExperienceExtractor {

	static final String PROMPT = """
		You extract fields for a personal review-campaign tracker from one screenshot.
		Return a JSON object with exactly these keys:
		- name: the large campaign title. Ignore site chrome and navigation. null if there is no title.
		- experienceType: VISIT when a tag says 방문형, DELIVERY when it says 배송형, PRESS when it says 기자단. null if none of those tags are visible.
		- reviewDeadline: the last day the user must publish the review, as YYYY-MM-DD. This is the review deadline, not the application deadline and not the winner announcement. If the page shows a range for writing or submitting the review, such as 리뷰 기간, 리뷰 등록 기간, or 콘텐츠 등록 기간, use the end date. A two-digit year such as 26 means 2026. A single date labeled 리뷰 마감 or 콘텐츠 등록 마감 is that day. 신청 마감, 모집 마감, 지원 마감, and 신청 기간 are application deadlines and must not be used. 발표, 발표 날짜, 선정, and 당첨 are when winners are announced and must not be used. Do not choose a date from color or calendar position alone. null when the review deadline cannot be told apart from those other dates.
		- platformNames: every place the user must publish the review. Sites show this differently. It may be a chip under the title, a sentence, or a label such as 블로그, 네이버 블로그, or 네이버블로그. It may also be only a channel icon, with no text, in the row under the campaign title. If text is visible, copy that text. If a recognizable channel icon has no text, use the channel it depicts, such as 네이버 블로그, 인스타그램, 유튜브, 릴스, 숏츠, or 클립. A green rounded square with a white speech bubble is 네이버 블로그. Do not add a channel that has no icon and no text. A category or status chip beside the icon, such as 방문형, 배송형, 기자단, 맛집, 카페, or 예약필수, is not a channel and does not replace the icon. Empty array only when no channel text and no channel icon are visible.
		- detailLink: the address-bar URL of the campaign page, including https://. null if no URL is visible.
		Do not include a reservation date, reservation time, visit schedule, reward, or any other key.
		""";

	private static final int NAME_MAX_LENGTH = 200;
	private static final int DETAIL_LINK_MAX_LENGTH = 1000;
	private static final int PLATFORM_NAME_MAX_LENGTH = 80;
	private static final int PLATFORM_NAME_MAX_COUNT = 20;
	private static final Pattern HTTP_URL = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);
	private static final Pattern BARE_URL = Pattern.compile(
		"^(?:www\\.)?[a-z0-9.-]+\\.[a-z]{2,}(?:/\\S*)?$",
		Pattern.CASE_INSENSITIVE
	);
	private static final Set<String> NOT_CHANNELS = Set.of(
		"방문형",
		"배송형",
		"기자단",
		"맛집",
		"카페",
		"뷰티",
		"cp",
		"지역",
		"예약필수",
		"예약 필수"
	);
	private static final Logger log = LoggerFactory.getLogger(OpenAiVisionExperienceExtractor.class);

	private final OpenAiProperties properties;
	private final JsonMapper jsonMapper;
	private final RestClient restClient;

	@Autowired
	public OpenAiVisionExperienceExtractor(OpenAiProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(5));
		requestFactory.setReadTimeout(Duration.ofSeconds(60));
		this.properties = properties;
		this.jsonMapper = new JsonMapper();
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	OpenAiVisionExperienceExtractor(OpenAiProperties properties, RestClient restClient) {
		this.properties = properties;
		this.jsonMapper = new JsonMapper();
		this.restClient = restClient;
	}

	@Override
	public ExtractedExperienceDraft extract(ScreenshotImage image) {
		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			throw new DomainException("OpenAI is not configured");
		}
		if (image == null || image.bytes() == null || image.bytes().length == 0) {
			throw new DomainException("image is required");
		}

		String mediaType = image.mediaType() == null || image.mediaType().isBlank()
			? "image/png"
			: image.mediaType();
		String dataUrl = "data:" + mediaType + ";base64," + Base64.getEncoder().encodeToString(image.bytes());

		Map<String, Object> textPart = new LinkedHashMap<>();
		textPart.put("type", "text");
		textPart.put("text", PROMPT);

		Map<String, Object> imageUrl = new LinkedHashMap<>();
		imageUrl.put("url", dataUrl);
		imageUrl.put("detail", "high");

		Map<String, Object> imagePart = new LinkedHashMap<>();
		imagePart.put("type", "image_url");
		imagePart.put("image_url", imageUrl);

		Map<String, Object> message = new LinkedHashMap<>();
		message.put("role", "user");
		message.put("content", List.of(textPart, imagePart));

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("model", properties.getModel());
		body.put("temperature", 0);
		body.put("max_tokens", 600);
		body.put("messages", List.of(message));
		body.put("response_format", Map.of("type", "json_object"));

		String responseBody;
		try {
			responseBody = restClient.post()
				.uri(properties.getBaseUrl() + "/v1/chat/completions")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey().trim())
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(String.class);
		}
		catch (RestClientResponseException ex) {
			log.warn("OpenAI vision call failed: status={}", ex.getStatusCode().value());
			throw new DomainException("screenshot extraction failed");
		}
		catch (RestClientException ex) {
			log.warn("OpenAI vision call failed");
			throw new DomainException("screenshot extraction failed");
		}

		return parse(responseBody);
	}

	private ExtractedExperienceDraft parse(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			throw new DomainException("screenshot extraction failed");
		}
		try {
			JsonNode root = jsonMapper.readTree(responseBody);
			JsonNode content = root.path("choices").path(0).path("message").path("content");
			if (!content.isString() || content.stringValue().isBlank()) {
				throw new DomainException("screenshot extraction failed");
			}
			JsonNode fields = jsonMapper.readTree(jsonObject(content.stringValue()));
			if (!fields.isObject()) {
				throw new DomainException("screenshot extraction failed");
			}
			return new ExtractedExperienceDraft(
				name(fields.get("name")),
				experienceType(fields.get("experienceType")),
				reviewDeadline(fields.get("reviewDeadline")),
				platformNames(fields.get("platformNames")),
				detailLink(fields.get("detailLink"))
			);
		}
		catch (JacksonException ex) {
			throw new DomainException("screenshot extraction failed");
		}
	}

	private static String jsonObject(String content) {
		String trimmed = content.trim();
		if (trimmed.startsWith("```")) {
			int start = trimmed.indexOf('\n');
			int end = trimmed.lastIndexOf("```");
			if (start >= 0 && end > start) {
				trimmed = trimmed.substring(start + 1, end).trim();
			}
		}
		int open = trimmed.indexOf('{');
		int close = trimmed.lastIndexOf('}');
		if (open >= 0 && close > open) {
			return trimmed.substring(open, close + 1);
		}
		return trimmed;
	}

	private static String name(JsonNode node) {
		String value = text(node);
		if (value == null) {
			return null;
		}
		if (value.length() > NAME_MAX_LENGTH) {
			return value.substring(0, NAME_MAX_LENGTH);
		}
		return value;
	}

	private static ExperienceType experienceType(JsonNode node) {
		String value = text(node);
		if (value == null) {
			return null;
		}
		try {
			return ExperienceType.valueOf(value.toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static LocalDate reviewDeadline(JsonNode node) {
		String value = text(node);
		if (value == null || value.length() < 10) {
			return null;
		}
		if (value.length() > 10) {
			char separator = value.charAt(10);
			if (separator != 'T' && separator != ' ') {
				return null;
			}
		}
		try {
			return LocalDate.parse(value.substring(0, 10));
		}
		catch (DateTimeParseException ex) {
			return null;
		}
	}

	private static List<String> platformNames(JsonNode node) {
		if (node == null || !node.isArray()) {
			return List.of();
		}
		Set<String> names = new LinkedHashSet<>();
		for (JsonNode item : node) {
			String value = text(item);
			if (value == null || value.length() > PLATFORM_NAME_MAX_LENGTH || !channel(value)) {
				continue;
			}
			names.add(value);
			if (names.size() == PLATFORM_NAME_MAX_COUNT) {
				break;
			}
		}
		return new ArrayList<>(names);
	}

	private static String detailLink(JsonNode node) {
		String value = text(node);
		if (value == null || value.length() > DETAIL_LINK_MAX_LENGTH) {
			return null;
		}
		if (HTTP_URL.matcher(value).matches()) {
			return value;
		}
		if (BARE_URL.matcher(value).matches()) {
			return "https://" + value;
		}
		return null;
	}

	private static boolean channel(String value) {
		String key = value.toLowerCase(Locale.ROOT).replace(" ", "");
		return !NOT_CHANNELS.contains(key);
	}

	private static String text(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode() || !node.isString()) {
			return null;
		}
		String value = node.stringValue().trim();
		return value.isEmpty() ? null : value;
	}
}
