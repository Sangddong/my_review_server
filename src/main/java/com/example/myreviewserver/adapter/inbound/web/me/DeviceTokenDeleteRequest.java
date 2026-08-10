package com.example.myreviewserver.adapter.inbound.web.me;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 삭제 요청")
public record DeviceTokenDeleteRequest(
	@Schema(description = "삭제할 푸시 토큰", example = "fcm-device-token-example")
	String token
) {
}
