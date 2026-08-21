package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.notification.ListNotificationsUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification inbox APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 */
@RestController
@RequestMapping("/api/me/notifications")
@Tag(name = "Me / Notifications", description = "로그인 사용자 알림 목록")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class NotificationController {

	private final ListNotificationsUseCase listNotificationsUseCase;

	public NotificationController(ListNotificationsUseCase listNotificationsUseCase) {
		this.listNotificationsUseCase = listNotificationsUseCase;
	}

	/** GET /api/me/notifications */
	@GetMapping
	@Operation(summary = "알림 목록 조회", description = "본인 알림을 최신순으로 반환합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = NotificationListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<NotificationResponse>> list() {
		Long userId = CurrentUser.requireUserId();
		List<NotificationResponse> notificationList = listNotificationsUseCase.execute(userId).stream()
			.map(NotificationResponse::from)
			.toList();
		return ApiResponse.ok(notificationList);
	}

	@Schema(name = "NotificationListApiResponse")
	private record NotificationListApiResponse(
		boolean success,
		List<NotificationResponse> data,
		String message
	) {
	}
}
