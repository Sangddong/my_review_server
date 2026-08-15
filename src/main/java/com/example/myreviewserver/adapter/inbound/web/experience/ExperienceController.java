package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.experience.ListExperiencesUseCase;
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
 * Experience HTTP APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @GetMapping: HTTP GET만 받음.
 */
@RestController
@RequestMapping("/api/experiences")
@Tag(name = "Experiences", description = "리뷰 체험")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class ExperienceController {

	private final ListExperiencesUseCase listExperiencesUseCase;

	public ExperienceController(ListExperiencesUseCase listExperiencesUseCase) {
		this.listExperiencesUseCase = listExperiencesUseCase;
	}

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
}
