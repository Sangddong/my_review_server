package com.example.myreviewserver.application.auth;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import org.springframework.stereotype.Service;

/**
 * Naver-specific login entry that verifies the provider code then delegates to SocialLoginUseCase.
 *
 * @Service: Spring이 이 클래스를 서비스 빈으로 등록해 컨트롤러에서 주입받을 수 있게 함.
 */
@Service
public class NaverLoginUseCase {

	private final NaverOAuthClient naverOAuthClient;
	private final SocialLoginUseCase socialLoginUseCase;

	public NaverLoginUseCase(NaverOAuthClient naverOAuthClient, SocialLoginUseCase socialLoginUseCase) {
		this.naverOAuthClient = naverOAuthClient;
		this.socialLoginUseCase = socialLoginUseCase;
	}

	public AuthTokenResult execute(String authorizationCode, String state) {
		if (authorizationCode == null || authorizationCode.isBlank()) {
			throw new DomainException("authorization code is required");
		}
		if (state == null || state.isBlank()) {
			throw new DomainException("state is required");
		}

		NaverUserProfile profile = naverOAuthClient.fetchUserProfile(authorizationCode.trim(), state.trim());
		return socialLoginUseCase.execute(new SocialLoginCommand(
			AuthProvider.NAVER,
			profile.providerUserId(),
			profile.email(),
			profile.nickname()
		));
	}
}
