package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.notification.ListNotificationsUseCase;
import com.example.myreviewserver.application.notification.MarkAllNotificationsReadUseCase;
import com.example.myreviewserver.application.notification.MarkNotificationReadUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification inbox APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @PatchMapping: HTTP PATCH만 받음.
 * @PathVariable: URL 경로의 {id}를 메서드 인자로 받음.
 */
@RestController
@RequestMapping("/api/me/notifications")
@Tag(name = "Me / Notifications", description = "로그인 사용자 알림 목록·읽음")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class NotificationController {

	private final ListNotificationsUseCase listNotificationsUseCase;
	private final MarkNotificationReadUseCase markNotificationReadUseCase;
	private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

	public NotificationController(
		ListNotificationsUseCase listNotificationsUseCase,
		MarkNotificationReadUseCase markNotificationReadUseCase,
		MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase
	) {
		this.listNotificationsUseCase = listNotificationsUseCase;
		this.markNotificationReadUseCase = markNotificationReadUseCase;
		this.markAllNotificationsReadUseCase = markAllNotificationsReadUseCase;
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

	/** PATCH /api/me/notifications/{id}/read */
	@PatchMapping("/{id}/read")
	@Operation(summary = "알림 단건 읽음", description = "본인 알림 하나를 읽음 처리합니다. 이미 읽은 알림도 성공입니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "읽음 처리 성공",
			content = @Content(schema = @Schema(implementation = NotificationApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "없거나 본인 알림이 아님"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(NotificationResponse.from(markNotificationReadUseCase.execute(userId, id)));
	}

	/** PATCH /api/me/notifications/read-all */
	@PatchMapping("/read-all")
	@Operation(summary = "알림 모두 읽음", description = "본인 미읽음 알림을 모두 읽음 처리합니다. 미읽음이 없어도 성공입니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "모두 읽음 처리 성공",
			content = @Content(schema = @Schema(implementation = NotificationReadAllApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<Void> markAllRead() {
		Long userId = CurrentUser.requireUserId();
		markAllNotificationsReadUseCase.execute(userId);
		return ApiResponse.ok(null);
	}

	@Schema(name = "NotificationListApiResponse")
	private record NotificationListApiResponse(
		boolean success,
		List<NotificationResponse> data,
		String message
	) {
	}

	@Schema(name = "NotificationApiResponse")
	private record NotificationApiResponse(
		boolean success,
		NotificationResponse data,
		String message
	) {
	}

	@Schema(name = "NotificationReadAllApiResponse")
	private record NotificationReadAllApiResponse(
		boolean success,
		Void data,
		String message
	) {
	}
}
