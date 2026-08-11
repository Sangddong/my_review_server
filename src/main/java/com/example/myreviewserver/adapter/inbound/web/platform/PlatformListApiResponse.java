package com.example.myreviewserver.adapter.inbound.web.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Swagger-only schema for GET /api/platforms success envelope.
 */
@Schema(name = "PlatformListApiResponse", description = "플랫폼 목록 성공 응답")
public record PlatformListApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "활성 플랫폼 목록")
	List<PlatformResponse> data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
