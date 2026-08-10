package com.example.myreviewserver.adapter.inbound.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger-only schema for social-login success envelope.
 * (Records cannot be subclassed, so OpenAPI uses this concrete type.)
 */
@Schema(name = "AuthLoginApiResponse", description = "소셜 로그인 성공 응답")
public record AuthLoginApiResponse(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "JWT 및 사용자 정보")
	AuthTokenResponse data,

	@Schema(description = "실패 메시지", nullable = true, example = "null")
	String message
) {
}
