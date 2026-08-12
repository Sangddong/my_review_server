package com.example.myreviewserver.adapter.inbound.web.platform;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랫폼 부분 수정 요청. name, color 중 하나 이상 필요")
public record UpdatePlatformRequest(
	@Schema(description = "플랫폼 이름", example = "블로그", nullable = true)
	String name,

	@Schema(description = "HEX 색상 코드", example = "#c6f8c8", nullable = true)
	String color
) {
}
