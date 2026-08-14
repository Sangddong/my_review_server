package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Swagger-only schema for GET /api/experiences success envelope.
 */
@Schema(name = "ExperienceListApiResponse", description = "체험 목록 성공 응답")
public record ExperienceListApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "체험 목록")
	List<ExperienceResponse> data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
