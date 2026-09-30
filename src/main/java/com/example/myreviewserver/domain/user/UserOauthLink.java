package com.example.myreviewserver.domain.user;

/**
 * Linked social login identity for a user.
 */
public record UserOauthLink(
	AuthProvider provider,
	String providerUserId,
	String refreshToken
) {
}
