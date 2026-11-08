package com.example.myreviewserver.adapter.outbound.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI credentials and model used for screenshot extraction.
 *
 * @ConfigurationProperties: application.properties의 app.openai.* 값을 이 객체 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {

	/**
	 * API key. Empty means screenshot extraction is unavailable.
	 */
	private String apiKey = "";

	/**
	 * Vision model. gpt-4o reads the small channel chips that gpt-4o-mini skips.
	 */
	private String model = "gpt-4o";

	/**
	 * API origin without a trailing path.
	 */
	private String baseUrl = "https://api.openai.com";

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModel() {
		return model == null || model.isBlank() ? "gpt-4o" : model.trim();
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBaseUrl() {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "https://api.openai.com";
		}
		String trimmed = baseUrl.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
