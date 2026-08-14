package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체험에 연결된 플랫폼")
public record ExperiencePlatformResponse(
	@Schema(description = "플랫폼 ID", example = "1")
	Long platformId,

	@Schema(description = "필수 여부", example = "true")
	boolean required,

	@Schema(description = "등록 완료 여부", example = "false")
	boolean registered
) {

	public static ExperiencePlatformResponse from(ExperiencePlatform platform) {
		return new ExperiencePlatformResponse(
			platform.getPlatformId(),
			platform.isRequired(),
			platform.isRegistered()
		);
	}
}
