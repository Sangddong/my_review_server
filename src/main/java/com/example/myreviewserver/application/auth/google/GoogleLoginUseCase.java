package com.example.myreviewserver.application.auth.google;

import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.SocialLoginCommand;
import com.example.myreviewserver.application.auth.SocialLoginUseCase;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import org.springframework.stereotype.Service;

/**
 * Google-specific login entry that verifies the provider code then delegates to SocialLoginUseCase.
 *
 * @Service: Spring이 이 클래스를 서비스 빈으로 등록해 컨트롤러에서 주입받을 수 있게 함.
 */
@Service
public class GoogleLoginUseCase {

	private final GoogleOAuthClient googleOAuthClient;
	private final SocialLoginUseCase socialLoginUseCase;

	public GoogleLoginUseCase(GoogleOAuthClient googleOAuthClient, SocialLoginUseCase socialLoginUseCase) {
		this.googleOAuthClient = googleOAuthClient;
		this.socialLoginUseCase = socialLoginUseCase;
	}

	public AuthTokenResult execute(String authorizationCode, String redirectUri) {
		if (authorizationCode == null || authorizationCode.isBlank()) {
			throw new DomainException("authorization code is required");
		}
		if (redirectUri == null || redirectUri.isBlank()) {
			throw new DomainException("redirectUri is required");
		}

		GoogleUserProfile profile = googleOAuthClient.fetchUserProfile(
			authorizationCode.trim(),
			redirectUri.trim()
		);
		return socialLoginUseCase.execute(new SocialLoginCommand(
			AuthProvider.GOOGLE,
			profile.providerUserId(),
			profile.email(),
			profile.nickname()
		));
	}
}
