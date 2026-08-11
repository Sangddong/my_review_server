package com.example.myreviewserver.adapter.inbound.web.platform;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.platform.CreatePlatformUseCase;
import com.example.myreviewserver.application.platform.ListPlatformsUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
	private final CreatePlatformUseCase createPlatformUseCase;

	public PlatformController(
		ListPlatformsUseCase listPlatformsUseCase,
		CreatePlatformUseCase createPlatformUseCase
	) {
		this.listPlatformsUseCase = listPlatformsUseCase;
		this.createPlatformUseCase = createPlatformUseCase;
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

	/**
	 * @PostMapping: HTTP POST만 받음.
	 * @RequestBody: JSON 본문을 CreatePlatformRequest로 변환.
	 * @ResponseStatus: 성공 시 HTTP 201.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "플랫폼 생성",
		description = """
			로그인한 사용자 플랫폼을 목록 맨 뒤에 추가합니다.
			같은 이름의 활성 플랫폼이 있으면 400입니다.
			soft delete된 동일 이름은 무시하고 새 행을 만듭니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "생성 성공",
			content = @Content(schema = @Schema(implementation = PlatformApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패 또는 이름 중복"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<PlatformResponse> create(@RequestBody CreatePlatformRequest request) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(PlatformResponse.from(
			createPlatformUseCase.execute(userId, request.name(), request.color())
		));
	}
}
