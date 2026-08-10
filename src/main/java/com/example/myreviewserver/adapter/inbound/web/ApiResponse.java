package com.example.myreviewserver.adapter.inbound.web;

/**
 * Minimal API response wrapper skeleton.
 * Concrete API payloads will be added in later issues.
 */
public record ApiResponse<T>(boolean success, T data, String message) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, null, message);
	}
}
