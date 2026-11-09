package com.example.myreviewserver.adapter.outbound.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.myreviewserver.application.experience.ExtractedExperienceDraft;
import com.example.myreviewserver.application.experience.ScreenshotImage;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiVisionExperienceExtractorTest {

	OpenAiProperties properties;
	MockRestServiceServer server;
	OpenAiVisionExperienceExtractor extractor;

	@BeforeEach
	void setUp() {
		properties = new OpenAiProperties();
		properties.setApiKey("super-secret-test-key");
		properties.setModel("gpt-4o-mini");
		properties.setBaseUrl("https://api.openai.com/");
		RestClient.Builder restClientBuilder = RestClient.builder();
		server = MockRestServiceServer.bindTo(restClientBuilder).build();
		extractor = new OpenAiVisionExperienceExtractor(properties, restClientBuilder.build());
	}

	@Test
	void readsFormFieldsAndDropsUncertainValues() {
		byte[] png = new byte[] {1, 2, 3, 4};
		server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header("Authorization", "Bearer super-secret-test-key"))
			.andExpect(content().string(Matchers.allOf(
				Matchers.containsString("\"model\":\"gpt-4o-mini\""),
				Matchers.containsString("신청 마감"),
				Matchers.containsString("리뷰 마감"),
				Matchers.containsString("reservation date"),
				Matchers.containsString("네이버 영수증 리뷰"),
				Matchers.containsString("data:image/png;base64," + Base64.getEncoder().encodeToString(png))
			)))
			.andRespond(withSuccess("""
				{"choices":[{"message":{"content":"{\\"name\\":\\"  버질스퀘어 성수  \\",\\"experienceType\\":\\"visit\\",\\"reviewDeadline\\":\\"2026-11-13T23:59:59\\",\\"platformNames\\":[\\"네이버 블로그\\",\\" \\",\\"네이버 블로그\\",\\"클립\\"],\\"detailLink\\":\\"javascript:alert(1)\\",\\"reservationDate\\":\\"2026-11-01\\"}"}}]}
				""", MediaType.APPLICATION_JSON));

		ExtractedExperienceDraft draft = extractor.extract(
			new ScreenshotImage(png, "image/png"),
			List.of("네이버 영수증 리뷰")
		);

		assertThat(draft.name()).isEqualTo("버질스퀘어 성수");
		assertThat(draft.experienceType()).isEqualTo(ExperienceType.VISIT);
		assertThat(draft.reviewDeadline()).isEqualTo(LocalDate.of(2026, 11, 13));
		assertThat(draft.platformNames()).containsExactly("네이버 블로그", "클립");
		assertThat(draft.detailLink()).isNull();
		server.verify();
	}

	@Test
	void returnsNullsWhenTheModelIsUnsure() {
		server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
			.andRespond(withSuccess("""
				{"choices":[{"message":{"content":"```json\\n{\\"name\\":null,\\"experienceType\\":\\"기자단\\",\\"reviewDeadline\\":\\"2026-13-40\\",\\"platformNames\\":null,\\"detailLink\\":null}\\n```"}}]}
				""", MediaType.APPLICATION_JSON));

		ExtractedExperienceDraft draft = extractor.extract(new ScreenshotImage(new byte[] {9}, "image/jpeg"), List.of());

		assertThat(draft.name()).isNull();
		assertThat(draft.experienceType()).isNull();
		assertThat(draft.reviewDeadline()).isNull();
		assertThat(draft.platformNames()).isEmpty();
		assertThat(draft.detailLink()).isNull();
		server.verify();
	}

	@Test
	void keepsAnHttpDetailLink() {
		server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
			.andRespond(withSuccess("""
				{"choices":[{"message":{"content":"{\\"name\\":\\"카페\\",\\"experienceType\\":\\"DELIVERY\\",\\"reviewDeadline\\":\\"2026-12-01\\",\\"platformNames\\":[],\\"detailLink\\":\\"https://example.com/c/1\\"}"}}]}
				""", MediaType.APPLICATION_JSON));

		ExtractedExperienceDraft draft = extractor.extract(new ScreenshotImage(new byte[] {9}, "image/png"), List.of());

		assertThat(draft.experienceType()).isEqualTo(ExperienceType.DELIVERY);
		assertThat(draft.detailLink()).isEqualTo("https://example.com/c/1");
		server.verify();
	}

	@Test
	void dropsCategoryTagsAndAddsHttpsToABareAddress() {
		server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
			.andRespond(withSuccess("""
				{"choices":[{"message":{"content":"{\\"name\\":\\"성수점\\",\\"experienceType\\":\\"VISIT\\",\\"reviewDeadline\\":\\"2026-11-22\\",\\"platformNames\\":[\\"블로그\\",\\"맛집\\",\\"CP\\",\\"방문형\\",\\"예약필수\\"],\\"detailLink\\":\\"reviewnote.co.kr/campaigns/1510284\\"}"}}]}
				""", MediaType.APPLICATION_JSON));

		ExtractedExperienceDraft draft = extractor.extract(new ScreenshotImage(new byte[] {9}, "image/png"), List.of());

		assertThat(draft.platformNames()).containsExactly("블로그");
		assertThat(draft.detailLink()).isEqualTo("https://reviewnote.co.kr/campaigns/1510284");
		server.verify();
	}

	@Test
	void failsWhenTheKeyIsMissing() {
		properties.setApiKey("  ");
		assertThatThrownBy(() -> extractor.extract(new ScreenshotImage(new byte[] {1}, "image/png"), List.of()))
			.isInstanceOf(DomainException.class)
			.hasMessage("OpenAI is not configured");
	}

	@Test
	void hidesTheUpstreamBodyWhenTheCallFails() {
		server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
			.andRespond(withServerError().body("super-secret-test-key leaked"));

		assertThatThrownBy(() -> extractor.extract(new ScreenshotImage(new byte[] {1}, "image/png"), List.of()))
			.isInstanceOf(DomainException.class)
			.hasMessage("screenshot extraction failed")
			.hasMessageNotContaining("super-secret-test-key")
			.hasMessageNotContaining("leaked");
		server.verify();
	}
}
