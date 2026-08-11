package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.devicetoken.DeleteDeviceTokenUseCase;
import com.example.myreviewserver.application.devicetoken.RegisterDeviceTokenUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Device token APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 */
@RestController
@RequestMapping("/api/me/device-tokens")
@Tag(name = "Me / Device Tokens", description = "로그인 사용자 푸시 디바이스 토큰 등록·삭제")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class DeviceTokenController {

	private final RegisterDeviceTokenUseCase registerDeviceTokenUseCase;
	private final DeleteDeviceTokenUseCase deleteDeviceTokenUseCase;

	public DeviceTokenController(
		RegisterDeviceTokenUseCase registerDeviceTokenUseCase,
		DeleteDeviceTokenUseCase deleteDeviceTokenUseCase
	) {
		this.registerDeviceTokenUseCase = registerDeviceTokenUseCase;
		this.deleteDeviceTokenUseCase = deleteDeviceTokenUseCase;
	}

	@PutMapping
	@Operation(summary = "디바이스 토큰 등록/갱신", description = "동일 token이 있으면 현재 사용자·platform으로 갱신합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "등록/갱신 성공",
			content = @Content(schema = @Schema(implementation = DeviceTokenUpsertApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<DeviceTokenResponse> upsert(@RequestBody DeviceTokenUpsertRequest request) {
		Long userId = CurrentUser.requireUserId();
		DeviceToken saved = registerDeviceTokenUseCase.execute(userId, request.token(), request.platform());
		return ApiResponse.ok(DeviceTokenResponse.from(saved));
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "디바이스 토큰 삭제", description = "본인 소유의 token만 삭제됩니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "없거나 본인 토큰이 아님"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public void delete(@RequestBody DeviceTokenDeleteRequest request) {
		Long userId = CurrentUser.requireUserId();
		deleteDeviceTokenUseCase.execute(userId, request.token());
	}

	@Schema(name = "DeviceTokenUpsertApiResponse")
	private record DeviceTokenUpsertApiResponse(
		boolean success,
		DeviceTokenResponse data,
		String message
	) {
	}
}
