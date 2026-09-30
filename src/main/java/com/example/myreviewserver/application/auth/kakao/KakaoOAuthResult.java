package com.example.myreviewserver.application.auth.kakao;

/**
 * Kakao OAuth exchange result: profile + tokens (unlink on withdraw uses Admin Key).
 */
public record KakaoOAuthResult(
	KakaoUserProfile profile,
	String accessToken,
	String refreshToken
) {
}
