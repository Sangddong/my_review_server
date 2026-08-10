package com.example.myreviewserver.adapter.inbound.web.auth;

import com.example.myreviewserver.application.auth.AuthTokenResult;

/**
 * API response payload after social login.
 */
public record AuthTokenResponse(
	String accessToken,
	String tokenType,
	long expiresInMs,
	Long userId,
	String nickname,
	boolean newlyRegistered
) {

	public static AuthTokenResponse from(AuthTokenResult result) {
		return new AuthTokenResponse(
			result.accessToken(),
			result.tokenType(),
			result.expiresInMs(),
			result.userId(),
			result.nickname(),
			result.newlyRegistered()
		);
	}
}
