package com.example.myreviewserver.adapter.inbound.web.platform;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.platform.ListPlatformsUseCase;
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
 * Platform HTTP APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @GetMapping: HTTP GET만 받음.
 */
@RestController
@RequestMapping("/api/platforms")
@Tag(name = "Platforms", description = "리뷰 플랫폼 목록")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class PlatformController {

	private final ListPlatformsUseCase listPlatformsUseCase;

	public PlatformController(ListPlatformsUseCase listPlatformsUseCase) {
		this.listPlatformsUseCase = listPlatformsUseCase;
	}

	@GetMapping
	@Operation(
		summary = "플랫폼 목록 조회",
		description = "로그인한 사용자의 활성(soft delete 되지 않은) 플랫폼만 sortOrder, id 오름차순으로 반환합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = PlatformListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<PlatformResponse>> list() {
		Long userId = CurrentUser.requireUserId();
		List<PlatformResponse> platforms = listPlatformsUseCase.execute(userId).stream()
			.map(PlatformResponse::from)
			.toList();
		return ApiResponse.ok(platforms);
	}
}
