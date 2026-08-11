package com.example.myreviewserver.adapter.inbound.web.platform;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플랫폼 생성 요청")
public record CreatePlatformRequest(
	@Schema(description = "플랫폼 이름", example = "블로그")
	String name,

	@Schema(description = "HEX 색상 코드", example = "#c6f8c8")
	String color
) {
}
