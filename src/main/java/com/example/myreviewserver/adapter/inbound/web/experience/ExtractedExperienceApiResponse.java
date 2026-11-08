package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger-only schema for POST /api/experiences/from-screenshot.
 */
@Schema(name = "ExtractedExperienceApiResponse", description = "스크린샷 추출 성공 응답")
public record ExtractedExperienceApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "추출된 입력값")
	ExtractedExperienceResponse data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
