package com.example.myreviewserver.application.auth;

/**
 * Verified profile fetched from Naver after exchanging the authorization code.
 */
public record NaverUserProfile(
	String providerUserId,
	String email,
	String nickname
) {
}
