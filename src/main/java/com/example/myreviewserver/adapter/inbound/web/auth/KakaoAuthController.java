package com.example.myreviewserver.adapter.inbound.web.auth;

import com.example.myreviewserver.adapter.inbound.web.ApiResponse;
import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.kakao.KakaoLoginUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kakao social login HTTP endpoint.
 *
 * @RestController: HTTP 요청을 받는 컨트롤러 + 응답을 JSON으로 반환.
 * @RequestMapping: 이 컨트롤러의 기본 URL prefix.
 */
@RestController
@RequestMapping("/api/auth")
public class KakaoAuthController {

	private final KakaoLoginUseCase kakaoLoginUseCase;

	public KakaoAuthController(KakaoLoginUseCase kakaoLoginUseCase) {
		this.kakaoLoginUseCase = kakaoLoginUseCase;
	}

	/**
	 * @PostMapping: HTTP POST만 받음.
	 * @RequestBody: JSON 본문을 KakaoLoginRequest로 변환.
	 */
	@PostMapping("/kakao")
	public ApiResponse<AuthTokenResponse> login(@RequestBody KakaoLoginRequest request) {
		AuthTokenResult result = kakaoLoginUseCase.execute(request.code(), request.redirectUri());
		return ApiResponse.ok(AuthTokenResponse.from(result));
	}
}
