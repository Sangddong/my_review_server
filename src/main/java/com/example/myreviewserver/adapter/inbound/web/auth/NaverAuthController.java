package com.example.myreviewserver.adapter.inbound.web.auth;

import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.NaverLoginUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Naver social login HTTP endpoint.
 *
 * @RestController: HTTP 요청을 받는 컨트롤러 + 응답을 JSON으로 반환.
 * @RequestMapping: 이 컨트롤러의 기본 URL prefix.
 */
@RestController
@RequestMapping("/api/auth")
public class NaverAuthController {

	private final NaverLoginUseCase naverLoginUseCase;

	public NaverAuthController(NaverLoginUseCase naverLoginUseCase) {
		this.naverLoginUseCase = naverLoginUseCase;
	}

	/**
	 * @PostMapping: HTTP POST만 받음.
	 * @RequestBody: JSON 본문을 NaverLoginRequest로 변환.
	 */
	@PostMapping("/naver")
	public ApiResponse<AuthTokenResponse> login(@RequestBody NaverLoginRequest request) {
		AuthTokenResult result = naverLoginUseCase.execute(request.code(), request.state());
		return ApiResponse.ok(AuthTokenResponse.from(result));
	}
}
