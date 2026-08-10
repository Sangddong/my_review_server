package com.example.myreviewserver.adapter.inbound.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ConfigurationProperties: binds app.jwt.* from application properties.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/**
	 * HMAC secret; must be at least 32 bytes for HS256.
	 */
	private String secret = "local-dev-only-change-me-32bytes-min!!";

	private long expirationMs = 86_400_000L;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public void setExpirationMs(long expirationMs) {
		this.expirationMs = expirationMs;
	}
}
