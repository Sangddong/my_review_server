package com.example.myreviewserver.adapter.outbound.google;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Google Cloud OAuth client credentials.
 *
 * @ConfigurationProperties: application.properties의 app.google.* 값을 이 객체 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.google")
public class GoogleProperties {

	/**
	 * OAuth 2.0 Client ID from Google Cloud Console.
	 */
	private String clientId = "";

	/**
	 * OAuth 2.0 Client Secret from Google Cloud Console.
	 */
	private String clientSecret = "";

	/**
	 * Allowed OAuth redirect URIs registered in Google Cloud Console.
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
