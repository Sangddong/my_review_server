package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.adapter.inbound.security.CurrentUser;
import com.example.myreviewserver.application.user.WithdrawUserUseCase;
import com.example.myreviewserver.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account-level APIs for the authenticated user.
 *
 * @RestController / @RequestMapping / @Tag: HTTP JSON API + Swagger 그룹.
 * @SecurityRequirement: Swagger Authorize(JWT) 필요.
 * @DeleteMapping: HTTP DELETE만 받음.
 * @ResponseStatus: 성공 시 HTTP 상태 코드를 지정.
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "로그인 사용자 계정")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class MeController {

	private final WithdrawUserUseCase withdrawUserUseCase;

	public MeController(WithdrawUserUseCase withdrawUserUseCase) {
		this.withdrawUserUseCase = withdrawUserUseCase;
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
}
