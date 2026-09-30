package com.example.myreviewserver.adapter.inbound.web.me;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 변경 요청")
public record MeNicknameUpdateRequest(
	@Schema(description = "새 닉네임 (1~100자)", example = "새닉네임", requiredMode = Schema.RequiredMode.REQUIRED)
	String nickname
) {
}
