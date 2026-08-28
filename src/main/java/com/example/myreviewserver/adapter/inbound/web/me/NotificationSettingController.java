package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.notification.GetNotificationSettingsUseCase;
import com.example.myreviewserver.application.notification.NotificationRuleSetting;
import com.example.myreviewserver.application.notification.UpdateNotificationSettingsUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import com.example.myreviewserver.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Per-rule push preference APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @GetMapping: HTTP GET만 받음.
 * @PatchMapping: HTTP PATCH만 받음.
 * @RequestBody: 요청 JSON 본문을 객체로 변환.
 */
@RestController
@RequestMapping("/api/me/notification-settings")
@Tag(name = "Me / Notification Settings", description = "로그인 사용자 푸시 종류별 수신 설정")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class NotificationSettingController {

	private final GetNotificationSettingsUseCase getNotificationSettingsUseCase;
	private final UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;

	public NotificationSettingController(
		GetNotificationSettingsUseCase getNotificationSettingsUseCase,
		UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase
	) {
		this.getNotificationSettingsUseCase = getNotificationSettingsUseCase;
		this.updateNotificationSettingsUseCase = updateNotificationSettingsUseCase;
	}

	/** GET /api/me/notification-settings */
	@GetMapping
	@Operation(
		summary = "푸시 수신 설정 조회",
		description = "모든 알림 규칙의 수신 여부를 반환합니다. 저장한 적 없는 규칙은 수신(on)입니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = NotificationSettingListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<NotificationSettingResponse>> list() {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(toResponseList(getNotificationSettingsUseCase.execute(userId)));
	}

	/** PATCH /api/me/notification-settings */
	@PatchMapping
	@Operation(
		summary = "푸시 수신 설정 변경",
		description = """
			보낸 규칙만 수신 여부를 변경하고 전체 설정을 반환합니다.
			끈 규칙은 푸시와 알림함이 모두 스킵되며, 다시 켜면 발송됩니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "변경 성공",
			content = @Content(schema = @Schema(implementation = NotificationSettingListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<NotificationSettingResponse>> update(
		@RequestBody NotificationSettingUpdateRequest request
	) {
		Long userId = CurrentUser.requireUserId();
		if (request == null || request.settingList() == null || request.settingList().isEmpty()) {
			throw new DomainException("settingList is required");
		}

		List<NotificationRuleSetting> settingList = new ArrayList<>();
		for (NotificationSettingUpdateRequest.Item item : request.settingList()) {
			if (item == null || item.ruleKey() == null) {
				throw new DomainException("ruleKey is required");
			}
			if (item.enabled() == null) {
				throw new DomainException("enabled is required");
			}
			settingList.add(new NotificationRuleSetting(item.ruleKey(), item.enabled()));
		}

		return ApiResponse.ok(toResponseList(updateNotificationSettingsUseCase.execute(userId, settingList)));
	}

	private static List<NotificationSettingResponse> toResponseList(List<NotificationRuleSetting> settingList) {
		return settingList.stream()
			.map(NotificationSettingResponse::from)
			.toList();
	}

	@Schema(name = "NotificationSettingListApiResponse")
	private record NotificationSettingListApiResponse(
		boolean success,
		List<NotificationSettingResponse> data,
		String message
	) {
	}
}
