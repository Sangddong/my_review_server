package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "등록된 디바이스 토큰")
public record DeviceTokenResponse(
	@Schema(description = "토큰 행 ID", example = "1")
	Long id,

	@Schema(description = "푸시 토큰")
	String token,

	@Schema(description = "플랫폼", example = "ANDROID")
	DevicePlatform platform
) {

	public static DeviceTokenResponse from(DeviceToken deviceToken) {
		return new DeviceTokenResponse(
			deviceToken.getId(),
			deviceToken.getToken(),
			deviceToken.getPlatform()
		);
	}
}
