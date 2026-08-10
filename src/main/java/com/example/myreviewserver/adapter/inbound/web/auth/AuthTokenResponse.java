package com.example.myreviewserver.adapter.inbound.web.auth;

import com.example.myreviewserver.application.auth.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API response payload after social login.
 */
@Schema(description = "소셜 로그인 성공 시 발급되는 JWT 및 사용자 요약")
public record AuthTokenResponse(
	@Schema(description = "API 인증용 JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
	String accessToken,

	@Schema(description = "토큰 타입", example = "Bearer")
	String tokenType,

	@Schema(description = "만료까지 남은 시간(ms)", example = "86400000")
	long expiresInMs,

	@Schema(description = "서버 사용자 ID", example = "1")
	Long userId,

	@Schema(description = "닉네임", example = "reviewer")
	String nickname,

	@Schema(description = "이번 요청에서 신규 가입 여부", example = "true")
	boolean newlyRegistered
) {

	public static AuthTokenResponse from(AuthTokenResult result) {
		return new AuthTokenResponse(
			result.accessToken(),
			result.tokenType(),
			result.expiresInMs(),
			result.userId(),
			result.nickname(),
			result.newlyRegistered()
		);
	}
}
