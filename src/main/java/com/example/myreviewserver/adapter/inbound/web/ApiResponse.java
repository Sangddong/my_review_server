package com.example.myreviewserver.adapter.inbound.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Minimal API response wrapper skeleton.
 * Concrete API payloads will be added in later issues.
 */
@Schema(description = "공통 API 응답 래퍼")
public record ApiResponse<T>(
	@Schema(description = "성공 여부", example = "true")
	boolean success,

	@Schema(description = "응답 데이터 (실패 시 null)")
	T data,

	@Schema(description = "실패 메시지 (성공 시 null)", example = "null", nullable = true)
	String message
) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, null, message);
	}
}
