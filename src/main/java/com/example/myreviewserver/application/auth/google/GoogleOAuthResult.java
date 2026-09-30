package com.example.myreviewserver.application.auth.google;

/**
 * Google OAuth exchange result: profile + tokens for later revoke on withdraw.
 */
public record GoogleOAuthResult(
	GoogleUserProfile profile,
	String accessToken,
	String refreshToken
) {
}
