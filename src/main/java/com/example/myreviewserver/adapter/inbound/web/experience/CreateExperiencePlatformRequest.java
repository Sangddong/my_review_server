package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체험에 연결할 플랫폼")
public record CreateExperiencePlatformRequest(
	@Schema(description = "플랫폼 ID", example = "10")
	Long platformId,

	@Schema(description = "필수 여부", example = "true")
	Boolean isRequired
) {
}
