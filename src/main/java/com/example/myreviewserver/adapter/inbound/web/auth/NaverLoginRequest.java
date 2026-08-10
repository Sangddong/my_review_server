package com.example.myreviewserver.adapter.inbound.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/auth/naver.
 * code/state come from the frontend Naver callback (localhost:5173).
 */
@Schema(description = "네이버 로그인 요청 (프론트 콜백의 code/state)")
public record NaverLoginRequest(
	@Schema(description = "네이버 인가 코드", example = "authorization-code-from-naver")
	String code,

	@Schema(description = "CSRF 방지용 state (인가 요청 때 보낸 값과 동일)", example = "random-state-token")
	String state
) {
}
