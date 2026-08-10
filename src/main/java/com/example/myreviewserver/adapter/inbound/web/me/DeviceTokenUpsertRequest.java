package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 등록/갱신 요청")
public record DeviceTokenUpsertRequest(
	@Schema(description = "FCM 등 푸시 토큰", example = "fcm-device-token-example")
	String token,

	@Schema(description = "디바이스 플랫폼", example = "ANDROID", allowableValues = {"ANDROID", "IOS", "WEB"})
	DevicePlatform platform
) {
}
