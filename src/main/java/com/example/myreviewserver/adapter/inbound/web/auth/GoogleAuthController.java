package com.example.myreviewserver.adapter.inbound.web.auth;

import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.google.GoogleLoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Google social login HTTP endpoint.
 *
 * @RestController: HTTP 요청을 받는 컨트롤러 + 응답을 JSON으로 반환.
 * @RequestMapping: 이 컨트롤러의 기본 URL prefix.
 * @Tag: Swagger UI에서 API를 묶는 그룹 이름.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "소셜 로그인 (JWT 발급). 인증 없이 호출합니다.")
public class GoogleAuthController {

	private final GoogleLoginUseCase googleLoginUseCase;

	public GoogleAuthController(GoogleLoginUseCase googleLoginUseCase) {
		this.googleLoginUseCase = googleLoginUseCase;
	}

	/**
	 * POST /api/auth/google
	 *
	 * @PostMapping: HTTP POST만 받음.
	 * @RequestBody: JSON 본문을 GoogleLoginRequest로 변환.
	 * @Operation: Swagger에 표시되는 API 요약/설명.
	 */
	@PostMapping("/google")
	@Operation(
		summary = "구글 로그인",
		description = """
			프론트 콜백에서 받은 `code`와, Google Cloud Console/서버 allowlist에 등록된 `redirectUri`로
			구글 프로필을 확인한 뒤 서버 JWT를 발급합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "로그인 성공",
			content = @Content(schema = @Schema(implementation = AuthLoginApiResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "잘못된 요청, redirectUri 불일치, 또는 구글 연동 실패"
		)
	})
	public ApiResponse<AuthTokenResponse> login(@RequestBody GoogleLoginRequest request) {
		AuthTokenResult result = googleLoginUseCase.execute(request.code(), request.redirectUri());
		return ApiResponse.ok(AuthTokenResponse.from(result));
	}
}
