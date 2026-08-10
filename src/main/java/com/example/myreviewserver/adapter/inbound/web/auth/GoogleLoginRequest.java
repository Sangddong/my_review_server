package com.example.myreviewserver.adapter.inbound.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/auth/google.
 * code/redirectUri come from the frontend Google OAuth callback.
 */
@Schema(description = "구글 로그인 요청 (프론트 콜백의 code + 등록된 redirectUri)")
public record GoogleLoginRequest(
	@Schema(description = "구글 인가 코드", example = "authorization-code-from-google")
	String code,

	@Schema(
		description = "Google Cloud Console에 등록한 Redirect URI (서버 allowlist와 일치해야 함)",
		example = "http://localhost:5173/auth/login/google/"
	)
	String redirectUri
) {
}
