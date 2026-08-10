package com.example.myreviewserver.adapter.outbound.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Naver developer console credentials.
 *
 * @ConfigurationProperties: application.properties의 app.naver.* 값을 이 객체 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.naver")
public class NaverProperties {

	/**
	 * Client ID from Naver Developers.
	 */
	private String clientId = "";

	/**
	 * Client Secret from Naver Developers.
	 */
	private String clientSecret = "";

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
}
