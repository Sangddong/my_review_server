package com.example.myreviewserver.application.auth.kakao;

/**
 * Verified profile fetched from Kakao after exchanging the authorization code.
 */
public record KakaoUserProfile(
	String providerUserId,
	String email,
	String nickname
) {
}
