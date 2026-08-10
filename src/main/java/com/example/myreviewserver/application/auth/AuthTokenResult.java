package com.example.myreviewserver.application.auth;

public record AuthTokenResult(
	String accessToken,
	String tokenType,
	long expiresInMs,
	Long userId,
	String nickname,
	boolean newlyRegistered
) {
}
