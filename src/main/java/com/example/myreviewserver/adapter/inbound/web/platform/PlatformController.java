package com.example.myreviewserver.adapter.inbound.web.platform;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.platform.CreatePlatformUseCase;
import com.example.myreviewserver.application.platform.DeletePlatformUseCase;
import com.example.myreviewserver.application.platform.ListPlatformsUseCase;
import com.example.myreviewserver.application.platform.ReorderPlatformsUseCase;
import com.example.myreviewserver.application.platform.UpdatePlatformUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	private final UpdatePlatformUseCase updatePlatformUseCase;
	private final DeletePlatformUseCase deletePlatformUseCase;
	private final ReorderPlatformsUseCase reorderPlatformsUseCase;

	public PlatformController(
		ListPlatformsUseCase listPlatformsUseCase,
		CreatePlatformUseCase createPlatformUseCase,
		UpdatePlatformUseCase updatePlatformUseCase,
		DeletePlatformUseCase deletePlatformUseCase,
		ReorderPlatformsUseCase reorderPlatformsUseCase
	) {
		this.listPlatformsUseCase = listPlatformsUseCase;
		this.createPlatformUseCase = createPlatformUseCase;
		this.updatePlatformUseCase = updatePlatformUseCase;
		this.deletePlatformUseCase = deletePlatformUseCase;
		this.reorderPlatformsUseCase = reorderPlatformsUseCase;
	}

	/** GET /api/platforms */
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
	 * POST /api/platforms
	 *
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

	/**
	 * PATCH /api/platforms/{id}
	 *
	 * @PatchMapping: HTTP PATCH만 받음.
	 * @PathVariable: URL 경로의 {id}를 메서드 인자로 받음.
	 * @RequestBody: JSON 본문을 UpdatePlatformRequest로 변환.
	 */
	@PatchMapping("/{id}")
	@Operation(
		summary = "플랫폼 수정",
		description = """
			로그인한 사용자의 활성 플랫폼 이름·색상을 부분 수정합니다.
			name, color 중 하나 이상 필요합니다.
			없거나 다른 사용자 것이거나 soft delete된 플랫폼은 400입니다.
			같은 이름의 다른 활성 플랫폼이 있으면 400입니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "수정 성공",
			content = @Content(schema = @Schema(implementation = PlatformApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "검증 실패, 이름 중복, 없거나 삭제된 플랫폼"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<PlatformResponse> update(
		@PathVariable Long id,
		@RequestBody UpdatePlatformRequest request
	) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(PlatformResponse.from(
			updatePlatformUseCase.execute(userId, id, request.name(), request.color())
		));
	}

	/**
	 * DELETE /api/platforms/{id}
	 *
	 * @DeleteMapping: HTTP DELETE만 받음.
	 * @PathVariable: URL 경로의 {id}를 메서드 인자로 받음.
	 * @ResponseStatus: 성공 시 HTTP 204.
	 */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
		summary = "플랫폼 삭제",
		description = """
			로그인한 사용자의 활성 플랫폼을 soft delete합니다. 행은 남기고 is_deleted만 표시합니다.
			체험에 연결된 데이터는 그대로 둡니다.
			없거나 다른 사용자 것이거나 이미 삭제된 플랫폼은 400입니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "없거나 이미 삭제된 플랫폼"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public void delete(@PathVariable Long id) {
		Long userId = CurrentUser.requireUserId();
		deletePlatformUseCase.execute(userId, id);
	}

	/**
	 * PUT /api/platforms/reorder
	 *
	 * @PutMapping: HTTP PUT만 받음.
	 * @RequestBody: JSON 본문을 ReorderPlatformsRequest로 변환.
	 */
	@PutMapping("/reorder")
	@Operation(
		summary = "플랫폼 정렬",
		description = """
			로그인한 사용자의 활성 플랫폼 표시 순서를 변경합니다.
			orderedIds는 활성 플랫폼 id를 빠짐없이, 중복 없이 원하는 순서로 보내야 합니다.
			soft delete된 플랫폼은 목록에 넣으면 안 됩니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "정렬 성공",
			content = @Content(schema = @Schema(implementation = PlatformListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "orderedIds가 활성 플랫폼과 일치하지 않음"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<PlatformResponse>> reorder(@RequestBody ReorderPlatformsRequest request) {
		Long userId = CurrentUser.requireUserId();
		List<PlatformResponse> platforms = reorderPlatformsUseCase.execute(userId, request.orderedIds()).stream()
			.map(PlatformResponse::from)
			.toList();
		return ApiResponse.ok(platforms);
	}
}
