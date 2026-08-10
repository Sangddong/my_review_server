package com.example.myreviewserver.application.auth;

import com.example.myreviewserver.domain.user.AuthProvider;

/**
 * Input for the common social-login use case.
 * Provider adapters (#13~15) will build this after verifying the external token.
 */
public record SocialLoginCommand(
	AuthProvider provider,
	String providerUserId,
	String email,
	String nickname
) {
}
