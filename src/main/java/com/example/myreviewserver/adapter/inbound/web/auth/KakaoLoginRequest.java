package com.example.myreviewserver.adapter.inbound.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/auth/kakao.
 * code/redirectUri come from the frontend Kakao callback.
 */
@Schema(description = "카카오 로그인 요청 (프론트 콜백의 code + 등록된 redirectUri)")
public record KakaoLoginRequest(
	@Schema(description = "카카오 인가 코드", example = "authorization-code-from-kakao")
	String code,

	@Schema(
		description = "카카오 콘솔에 등록한 Redirect URI (서버 allowlist와 일치해야 함)",
		example = "http://localhost:5173/auth/login/kakao/"
	)
	String redirectUri
) {
}
