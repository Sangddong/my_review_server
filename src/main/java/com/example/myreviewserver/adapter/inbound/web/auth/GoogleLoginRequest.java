package com.example.myreviewserver.adapter.inbound.web.auth;

/**
 * Request body for POST /api/auth/google.
 * code/redirectUri come from the frontend Google OAuth callback.
 */
public record GoogleLoginRequest(
	String code,
	String redirectUri
) {
}
