package com.example.myreviewserver.application.auth.google;

/**
 * Verified profile fetched from Google after exchanging the authorization code.
 */
public record GoogleUserProfile(
	String providerUserId,
	String email,
	String nickname
) {
}
