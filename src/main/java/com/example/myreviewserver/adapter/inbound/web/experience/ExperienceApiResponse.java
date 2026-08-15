package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger-only schema for GET /api/experiences/{id} success envelope.
 */
@Schema(name = "ExperienceApiResponse", description = "체험 단건 성공 응답")
public record ExperienceApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "체험")
	ExperienceResponse data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
