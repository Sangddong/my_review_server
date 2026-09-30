package com.example.myreviewserver.adapter.outbound.auth;

import com.example.myreviewserver.application.auth.google.GoogleOAuthClient;
import com.example.myreviewserver.application.auth.kakao.KakaoOAuthClient;
import com.example.myreviewserver.application.auth.naver.NaverOAuthClient;
import com.example.myreviewserver.application.user.SocialAccountUnlinkClient;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.UserOauthLink;
import org.springframework.stereotype.Component;

/**
 * Routes provider unlink to Kakao / Naver / Google adapters.
 *
 * @Component: 스프링 빈.
 */
@Component
public class SocialAccountUnlinkClientAdapter implements SocialAccountUnlinkClient {

	private final KakaoOAuthClient kakaoOAuthClient;
	private final NaverOAuthClient naverOAuthClient;
	private final GoogleOAuthClient googleOAuthClient;

	public SocialAccountUnlinkClientAdapter(
		KakaoOAuthClient kakaoOAuthClient,
		NaverOAuthClient naverOAuthClient,
		GoogleOAuthClient googleOAuthClient
	) {
		this.kakaoOAuthClient = kakaoOAuthClient;
		this.naverOAuthClient = naverOAuthClient;
		this.googleOAuthClient = googleOAuthClient;
	}

	@Override
	public void unlink(UserOauthLink link) {
		if (link == null || link.provider() == null) {
			throw new DomainException("provider is required");
		}
		switch (link.provider()) {
			case KAKAO -> {
				if (link.providerUserId() == null || link.providerUserId().isBlank()) {
					throw new DomainException("providerUserId is required");
				}
				kakaoOAuthClient.unlink(link.providerUserId().trim());
			}
			case NAVER -> {
				if (link.refreshToken() == null || link.refreshToken().isBlank()) {
					throw new DomainException("Naver refresh token is missing; login again then withdraw");
				}
				naverOAuthClient.unlink(link.refreshToken().trim(), true);
			}
			case GOOGLE -> {
				if (link.refreshToken() == null || link.refreshToken().isBlank()) {
					throw new DomainException("Google refresh token is missing; login again then withdraw");
				}
				googleOAuthClient.unlink(link.refreshToken().trim());
			}
		}
	}
}
