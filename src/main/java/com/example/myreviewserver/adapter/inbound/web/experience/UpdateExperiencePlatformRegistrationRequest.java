package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체험 플랫폼 등록 완료/해제 요청")
public record UpdateExperiencePlatformRegistrationRequest(
	@Schema(description = "등록 완료 여부", example = "true")
	Boolean registered
) {
}
