package com.example.myreviewserver.adapter.outbound.auth;

import com.example.myreviewserver.application.auth.google.GoogleOAuthClient;
import com.example.myreviewserver.application.auth.kakao.KakaoOAuthClient;
import com.example.myreviewserver.application.auth.naver.NaverOAuthClient;
import com.example.myreviewserver.application.user.SocialAccountUnlinkClient;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.UserOauthLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Routes provider unlink to Kakao / Naver / Google adapters.
 * Missing Naver/Google refresh tokens skip provider revoke so local withdraw still succeeds.
 *
 * @Component: 스프링 빈.
 */
@Component
public class SocialAccountUnlinkClientAdapter implements SocialAccountUnlinkClient {

	private static final Logger log = LoggerFactory.getLogger(SocialAccountUnlinkClientAdapter.class);

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
					log.warn("Skipping Naver revoke: refresh token missing for providerUserId={}",
						link.providerUserId());
					return;
				}
				try {
					naverOAuthClient.unlink(link.refreshToken().trim(), true);
				}
				catch (DomainException ex) {
					log.warn("Naver revoke failed for providerUserId={}: {}",
						link.providerUserId(), ex.getMessage());
				}
			}
			case GOOGLE -> {
				if (link.refreshToken() == null || link.refreshToken().isBlank()) {
					log.warn("Skipping Google revoke: token missing for providerUserId={}",
						link.providerUserId());
					return;
				}
				try {
					googleOAuthClient.unlink(link.refreshToken().trim());
				}
				catch (DomainException ex) {
					// Expired access_token 등으로 revoke 실패해도 로컬 탈퇴는 진행.
					log.warn("Google revoke failed for providerUserId={}: {}",
						link.providerUserId(), ex.getMessage());
				}
			}
		}
	}
}
