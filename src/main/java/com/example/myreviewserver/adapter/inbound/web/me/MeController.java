package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.user.GetMyProfileUseCase;
import com.example.myreviewserver.application.user.UpdateMyNicknameUseCase;
import com.example.myreviewserver.application.user.WithdrawUserUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import com.example.myreviewserver.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account-level APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @GetMapping / @PatchMapping / @DeleteMapping: 각각 해당 HTTP 메서드만 받음.
 * @RequestBody: 요청 JSON 본문을 객체로 변환.
 * @ResponseStatus: 성공 시 HTTP 상태 코드를 지정.
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "로그인 사용자 계정")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class MeController {

	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UpdateMyNicknameUseCase updateMyNicknameUseCase;
	private final WithdrawUserUseCase withdrawUserUseCase;

	public MeController(
		GetMyProfileUseCase getMyProfileUseCase,
		UpdateMyNicknameUseCase updateMyNicknameUseCase,
		WithdrawUserUseCase withdrawUserUseCase
	) {
		this.getMyProfileUseCase = getMyProfileUseCase;
		this.updateMyNicknameUseCase = updateMyNicknameUseCase;
		this.withdrawUserUseCase = withdrawUserUseCase;
	}

	/** GET /api/me — 내 프로필 */
	@GetMapping
	@Operation(summary = "내 프로필 조회", description = "로그인 사용자의 id, email, nickname, 가입·로그인 시각을 반환합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = MeProfileApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<MeProfileResponse> getProfile() {
		Long userId = CurrentUser.requireUserId();
		return ApiResponse.ok(MeProfileResponse.from(getMyProfileUseCase.execute(userId)));
	}

	/** PATCH /api/me — 닉네임 변경 */
	@PatchMapping
	@Operation(
		summary = "닉네임 변경",
		description = """
			닉네임을 변경하고 갱신된 프로필을 반환합니다.
			JWT 안의 nickname claim은 로그인 시점 값이라 바로 바뀌지 않습니다.
			화면에는 이 응답(또는 이후 GET /api/me)을 쓰면 됩니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "변경 성공",
			content = @Content(schema = @Schema(implementation = MeProfileApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public ApiResponse<MeProfileResponse> updateNickname(@RequestBody MeNicknameUpdateRequest request) {
		Long userId = CurrentUser.requireUserId();
		if (request == null || request.nickname() == null) {
			throw new DomainException("nickname is required");
		}
		return ApiResponse.ok(MeProfileResponse.from(updateMyNicknameUseCase.execute(userId, request.nickname())));
	}

	/** DELETE /api/me — 회원 탈퇴 (soft delete) */
	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
		summary = "회원 탈퇴",
		description = """
			계정을 soft delete 합니다. 푸시 기기 토큰과 소셜 로그인 연동을 즉시 해제합니다.
			체험·플랫폼 등 나머지 데이터는 보관 기간 후 스케줄러가 hard delete 합니다.
			탈퇴 후 같은 JWT는 더 이상 인증되지 않으며, 같은 소셜 계정으로 재가입할 수 있습니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "탈퇴 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 탈퇴했거나 사용자 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 필요")
	})
	public void withdraw() {
		Long userId = CurrentUser.requireUserId();
		withdrawUserUseCase.execute(userId);
	}

	@Schema(name = "MeProfileApiResponse")
	private record MeProfileApiResponse(
		boolean success,
		MeProfileResponse data,
		String message
	) {
	}
}
