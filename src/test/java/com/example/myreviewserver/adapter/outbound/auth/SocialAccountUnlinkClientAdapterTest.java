package com.example.myreviewserver.adapter.outbound.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.myreviewserver.application.auth.google.GoogleOAuthClient;
import com.example.myreviewserver.application.auth.kakao.KakaoOAuthClient;
import com.example.myreviewserver.application.auth.naver.NaverOAuthClient;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.UserOauthLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SocialAccountUnlinkClientAdapterTest {

	@Mock
	KakaoOAuthClient kakaoOAuthClient;
	@Mock
	NaverOAuthClient naverOAuthClient;
	@Mock
	GoogleOAuthClient googleOAuthClient;

	@InjectMocks
	SocialAccountUnlinkClientAdapter adapter;

	@Test
	void googleWithoutRefreshToken_skipsRevoke() {
		UserOauthLink link = new UserOauthLink(AuthProvider.GOOGLE, "g-1", null);

		assertDoesNotThrow(() -> adapter.unlink(link));

		verify(googleOAuthClient, never()).unlink(anyString());
		verifyNoInteractions(kakaoOAuthClient, naverOAuthClient);
	}

	@Test
	void googleWithRefreshToken_revokes() {
		UserOauthLink link = new UserOauthLink(AuthProvider.GOOGLE, "g-1", "rt-google");

		adapter.unlink(link);

		verify(googleOAuthClient).unlink("rt-google");
	}

	@Test
	void googleRevokeFailure_doesNotThrow() {
		UserOauthLink link = new UserOauthLink(AuthProvider.GOOGLE, "g-1", "expired-token");
		doThrow(new DomainException("Failed to unlink Google account"))
			.when(googleOAuthClient).unlink("expired-token");

		assertDoesNotThrow(() -> adapter.unlink(link));

		verify(googleOAuthClient).unlink("expired-token");
	}

	@Test
	void naverWithoutRefreshToken_skipsRevoke() {
		UserOauthLink link = new UserOauthLink(AuthProvider.NAVER, "n-1", "   ");

		assertDoesNotThrow(() -> adapter.unlink(link));

		verify(naverOAuthClient, never()).unlink(anyString(), anyBoolean());
	}
}
