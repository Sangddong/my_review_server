package com.example.myreviewserver.application.auth.naver;

/**
 * Naver OAuth exchange result: profile + tokens for later revoke on withdraw.
 */
public record NaverOAuthResult(
	NaverUserProfile profile,
	String accessToken,
	String refreshToken
) {
}
