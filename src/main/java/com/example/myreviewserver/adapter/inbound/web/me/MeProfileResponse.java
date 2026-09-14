package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "로그인 사용자 프로필")
public record MeProfileResponse(
	@Schema(description = "사용자 ID", example = "1")
	Long id,

	@Schema(description = "이메일 (소셜에서 없을 수 있음)", example = "user@example.com", nullable = true)
	String email,

	@Schema(description = "닉네임", example = "리뷰어")
	String nickname,

	@Schema(description = "마지막 로그인 시각", nullable = true)
	Instant lastLoginAt,

	@Schema(description = "가입 시각")
	Instant createdAt
) {

	public static MeProfileResponse from(User user) {
		return new MeProfileResponse(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getLastLoginAt(),
			user.getCreatedAt()
		);
	}
}
