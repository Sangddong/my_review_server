package com.example.myreviewserver.adapter.inbound.web.platform;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger-only schema for a single-platform success envelope.
 */
@Schema(name = "PlatformApiResponse", description = "플랫폼 단건 성공 응답")
public record PlatformApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "플랫폼")
	PlatformResponse data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
