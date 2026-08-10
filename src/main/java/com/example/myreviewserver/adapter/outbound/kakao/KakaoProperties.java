package com.example.myreviewserver.adapter.outbound.kakao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kakao developer console credentials (REST API key + client secret).
 *
 * @ConfigurationProperties: application.properties의 app.kakao.* 값을 이 객체 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.kakao")
public class KakaoProperties {

	/**
	 * Kakao REST API key (used as OAuth client_id).
	 */
	private String clientId = "";

	/**
	 * Kakao client secret (required when Client Secret is ON in console).
	 */
	private String clientSecret = "";

	/**
	 * Allowed OAuth redirect URIs registered in Kakao Developers console.
	 */
	private List<String> redirectUris = new ArrayList<>();

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public List<String> getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(List<String> redirectUris) {
		this.redirectUris = redirectUris != null ? redirectUris : new ArrayList<>();
	}
}
