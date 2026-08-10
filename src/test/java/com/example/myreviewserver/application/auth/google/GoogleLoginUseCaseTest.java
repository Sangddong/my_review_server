package com.example.myreviewserver.application.auth.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.SocialLoginCommand;
import com.example.myreviewserver.application.auth.SocialLoginUseCase;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleLoginUseCaseTest {

	@Mock
	GoogleOAuthClient googleOAuthClient;

	@Mock
	SocialLoginUseCase socialLoginUseCase;

	@InjectMocks
	GoogleLoginUseCase googleLoginUseCase;

	@Test
	void exchangesCodeThenDelegatesToSocialLogin() {
		when(googleOAuthClient.fetchUserProfile("code-1", "http://localhost:5173/auth/login/google/"))
			.thenReturn(new GoogleUserProfile("google-99", "a@test.com", "alice"));
		when(socialLoginUseCase.execute(any())).thenReturn(
			new AuthTokenResult("jwt", "Bearer", 1000L, 1L, "alice", true)
		);

		AuthTokenResult result = googleLoginUseCase.execute(
			"code-1",
			"http://localhost:5173/auth/login/google/"
		);

		assertThat(result.accessToken()).isEqualTo("jwt");
		assertThat(result.newlyRegistered()).isTrue();
		verify(socialLoginUseCase).execute(eq(
			new SocialLoginCommand(AuthProvider.GOOGLE, "google-99", "a@test.com", "alice")
		));
	}

	@Test
	void rejectsMissingRedirectUri() {
		assertThatThrownBy(() -> googleLoginUseCase.execute("code", " "))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("redirectUri");
	}
}
