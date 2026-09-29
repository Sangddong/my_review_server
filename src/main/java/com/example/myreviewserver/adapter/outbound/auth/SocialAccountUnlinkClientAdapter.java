package com.example.myreviewserver.adapter.outbound.auth;

import com.example.myreviewserver.application.auth.kakao.KakaoOAuthClient;
import com.example.myreviewserver.application.user.SocialAccountUnlinkClient;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Routes provider unlink to the matching OAuth adapter.
 * Kakao: Admin Key unlink. Naver/Google: not supported yet (no stored tokens).
 *
 * @Component: 스프링 빈.
 */
@Component
public class SocialAccountUnlinkClientAdapter implements SocialAccountUnlinkClient {

	private static final Logger log = LoggerFactory.getLogger(SocialAccountUnlinkClientAdapter.class);

	private final KakaoOAuthClient kakaoOAuthClient;

	public SocialAccountUnlinkClientAdapter(KakaoOAuthClient kakaoOAuthClient) {
		this.kakaoOAuthClient = kakaoOAuthClient;
	}

	@Override
	public void unlink(AuthProvider provider, String providerUserId) {
		if (provider == null) {
			throw new DomainException("provider is required");
		}
		if (providerUserId == null || providerUserId.isBlank()) {
			throw new DomainException("providerUserId is required");
		}
		switch (provider) {
			case KAKAO -> kakaoOAuthClient.unlink(providerUserId.trim());
			case NAVER, GOOGLE -> log.warn(
				"Provider unlink is not implemented for {}; local oauth row will still be removed",
				provider
			);
		}
	}
}
