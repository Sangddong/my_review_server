package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.experience.CreateExperienceUseCase;
import com.example.myreviewserver.application.experience.DeleteExperienceUseCase;
import com.example.myreviewserver.application.experience.GetExperienceUseCase;
import com.example.myreviewserver.application.experience.ListExperiencesUseCase;
import com.example.myreviewserver.application.experience.UpdateExperienceUseCase;
import com.example.myreviewserver.application.experience.UpdateExperiencePlatformRegistrationUseCase;
import com.example.myreviewserver.application.experience.UpdateExperienceSubmissionUseCase;
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
 * Experience HTTP APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @GetMapping: HTTP GET만 받음.
 * @PostMapping: HTTP POST만 받음.
 * @PatchMapping: HTTP PATCH만 받음.
 * @DeleteMapping: HTTP DELETE만 받음.
 * @PutMapping: HTTP PUT만 받음.
 * @PathVariable: URL 경로의 {id}를 메서드 인자로 받음.
 * @RequestBody: JSON 본문을 요청 객체로 변환.
 * @ResponseStatus: 성공 시 HTTP 상태 코드 지정.
 */
@RestController
@RequestMapping("/api/experiences")
@Tag(name = "Experiences", description = "리뷰 체험")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class ExperienceController {

	private final ListExperiencesUseCase listExperiencesUseCase;
	private final GetExperienceUseCase getExperienceUseCase;
	private final CreateExperienceUseCase createExperienceUseCase;
	private final UpdateExperienceUseCase updateExperienceUseCase;
	private final DeleteExperienceUseCase deleteExperienceUseCase;
	private final UpdateExperiencePlatformRegistrationUseCase updateExperiencePlatformRegistrationUseCase;
	private final UpdateExperienceSubmissionUseCase updateExperienceSubmissionUseCase;

	public ExperienceController(
		ListExperiencesUseCase listExperiencesUseCase,
		GetExperienceUseCase getExperienceUseCase,
		CreateExperienceUseCase createExperienceUseCase,
		UpdateExperienceUseCase updateExperienceUseCase,
		DeleteExperienceUseCase deleteExperienceUseCase,
		UpdateExperiencePlatformRegistrationUseCase updateExperiencePlatformRegistrationUseCase,
		UpdateExperienceSubmissionUseCase updateExperienceSubmissionUseCase
	) {
		this.listExperiencesUseCase = listExperiencesUseCase;
		this.getExperienceUseCase = getExperienceUseCase;
		this.createExperienceUseCase = createExperienceUseCase;
		this.updateExperienceUseCase = updateExperienceUseCase;
		this.deleteExperienceUseCase = deleteExperienceUseCase;
		this.updateExperiencePlatformRegistrationUseCase = updateExperiencePlatformRegistrationUseCase;
		this.updateExperienceSubmissionUseCase = updateExperienceSubmissionUseCase;
	}

	/** GET /api/experiences/upcoming */
	@GetMapping("/upcoming")
	@Operation(
		summary = "다가오는 체험 목록",
		description = """
			로그인한 사용자 본인의 미제출 체험만 반환합니다.
			정렬은 reservationDate, reservationTime 오름차순(null은 뒤), id 오름차순입니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = ExperienceListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<ExperienceResponse>> upcoming() {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(listExperiencesUseCase.upcoming(userId).stream()
			.map(ExperienceResponse::from)
			.toList());
	}

	/** GET /api/experiences/completed */
	@GetMapping("/completed")
	@Operation(
		summary = "완료된 체험 목록",
		description = """
			로그인한 사용자 본인의 제출 완료 체험만 반환합니다.
			정렬은 reservationDate, reservationTime 오름차순(null은 뒤), id 오름차순입니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = ExperienceListApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<List<ExperienceResponse>> completed() {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(listExperiencesUseCase.completed(userId).stream()
			.map(ExperienceResponse::from)
			.toList());
	}

	/** GET /api/experiences/{id} */
	@GetMapping("/{id}")
	@Operation(
		summary = "체험 상세 조회",
		description = """
			로그인한 사용자 본인 소유의 체험만 조회합니다.
			없거나 다른 사용자 소유이면 Experience not found로 실패합니다.
			응답에 플랫폼·필수여부·등록완료·제출여부가 포함됩니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = ExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "없거나 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExperienceResponse> get(@PathVariable Long id) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(ExperienceResponse.from(getExperienceUseCase.get(userId, id)));
	}

	/** POST /api/experiences */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "체험 생성",
		description = """
			로그인한 사용자 본인 체험을 생성하고 플랫폼을 연결합니다.
			reviewDeadline과 platformList는 필수이며, 필수(isRequired=true) 플랫폼이 1개 이상이어야 합니다.
			연결할 platformId는 본인 활성 플랫폼이어야 합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "생성 성공",
			content = @Content(schema = @Schema(implementation = ExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExperienceResponse> create(@RequestBody CreateExperienceRequest request) {
		Long userId = CurrentUser.requireUserId();
		List<CreateExperienceUseCase.PlatformLink> platformList = request.platformList() == null
			? null
			: request.platformList().stream()
				.map(p -> new CreateExperienceUseCase.PlatformLink(p.platformId(), p.isRequired()))
				.toList();
		return ApiResponse.ok(ExperienceResponse.from(createExperienceUseCase.create(
			userId,
			request.name(),
			request.experienceType(),
			request.reservationDate(),
			request.reservationTime(),
			request.reviewDeadline(),
			request.detailLink(),
			platformList
		)));
	}

	/** PATCH /api/experiences/{id} */
	@PatchMapping("/{id}")
	@Operation(
		summary = "체험 편집",
		description = """
			로그인한 사용자 본인 소유의 체험 기본 필드와 플랫폼 구성을 부분 수정합니다.
			보낸 필드만 바뀌며, 제출 상태와 플랫폼 등록 완료 여부는 이 API에서 바꾸지 않습니다.
			platformList를 보내면 구성을 통째로 바꾸고, 남아 있는 플랫폼의 등록 상태는 유지합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "수정 성공",
			content = @Content(schema = @Schema(implementation = ExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패 또는 없거나 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExperienceResponse> update(
		@PathVariable Long id,
		@RequestBody UpdateExperienceRequest request
	) {
		Long userId = CurrentUser.requireUserId();
		List<UpdateExperienceUseCase.PlatformLink> platformList = request.platformList() == null
			? null
			: request.platformList().stream()
				.map(p -> new UpdateExperienceUseCase.PlatformLink(p.platformId(), p.isRequired()))
				.toList();
		return ApiResponse.ok(ExperienceResponse.from(updateExperienceUseCase.update(
			userId,
			id,
			request.name(),
			request.experienceType(),
			request.reservationDate(),
			request.reservationTime(),
			request.reviewDeadline(),
			request.detailLink(),
			platformList
		)));
	}

	/** DELETE /api/experiences/{id} */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
		summary = "체험 삭제",
		description = """
			로그인한 사용자 본인 소유의 체험을 hard delete합니다.
			연결된 experience_platforms와 등록 완료 행도 함께 삭제됩니다.
			없거나 다른 사용자 소유이면 Experience not found로 실패합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "없거나 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public void delete(@PathVariable Long id) {
		Long userId = CurrentUser.requireUserId();
		deleteExperienceUseCase.delete(userId, id);
	}

	/** PUT /api/experiences/{id}/platforms/{platformId}/registration */
	@PutMapping("/{id}/platforms/{platformId}/registration")
	@Operation(
		summary = "체험 플랫폼 등록 상태 변경",
		description = """
			로그인한 사용자 본인 소유의 체험에 연결된 플랫폼의 등록 완료/해제를 바꿉니다.
			registered=true이면 experience_registered_platforms 행이 생기고, false이면 삭제됩니다.
			체험에 연결되지 않은 platformId는 실패합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "변경 성공",
			content = @Content(schema = @Schema(implementation = ExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패 또는 없거나 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExperienceResponse> updatePlatformRegistration(
		@PathVariable Long id,
		@PathVariable Long platformId,
		@RequestBody UpdateExperiencePlatformRegistrationRequest request
	) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(ExperienceResponse.from(
			updateExperiencePlatformRegistrationUseCase.update(userId, id, platformId, request.registered())
		));
	}

	/** PATCH /api/experiences/{id}/submission */
	@PatchMapping("/{id}/submission")
	@Operation(
		summary = "체험 제출 상태 변경",
		description = """
			로그인한 사용자 본인 소유의 체험 리뷰 제출 여부를 바꿉니다.
			submitted=true이면 완료된 목록에, false이면 다가오는 목록에 나타납니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "변경 성공",
			content = @Content(schema = @Schema(implementation = ExperienceApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패 또는 없거나 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<ExperienceResponse> updateSubmission(
		@PathVariable Long id,
		@RequestBody UpdateExperienceSubmissionRequest request
	) {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(ExperienceResponse.from(
			updateExperienceSubmissionUseCase.update(userId, id, request.submitted())
		));
	}
}
