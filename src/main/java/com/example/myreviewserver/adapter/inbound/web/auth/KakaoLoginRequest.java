package com.example.myreviewserver.adapter.inbound.web.auth;

/**
 * Request body for POST /api/auth/kakao.
 * code/redirectUri come from the frontend Kakao callback.
 */
public record KakaoLoginRequest(
	String code,
	String redirectUri
) {
}
