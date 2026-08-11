package com.example.myreviewserver.adapter.inbound.web.platform;

import com.example.myreviewserver.domain.platform.Platform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랫폼 목록 항목")
public record PlatformResponse(
	@Schema(description = "플랫폼 ID", example = "1")
	Long id,

	@Schema(description = "이름", example = "블로그")
	String name,

	@Schema(description = "HEX 색상 코드", example = "#c6f8c8")
	String color,

	@Schema(description = "정렬 순서 (작을수록 앞)", example = "0")
	int sortOrder
) {

	public static PlatformResponse from(Platform platform) {
		return new PlatformResponse(
			platform.getId(),
			platform.getName(),
			platform.getColor(),
			platform.getSortOrder()
		);
	}
}
