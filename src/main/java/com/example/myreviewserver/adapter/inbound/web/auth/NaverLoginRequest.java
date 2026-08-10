package com.example.myreviewserver.adapter.inbound.web.auth;

/**
 * Request body for POST /api/auth/naver.
 * code/state come from the frontend Naver callback (localhost:5173).
 */
public record NaverLoginRequest(
	String code,
	String state
) {
}
