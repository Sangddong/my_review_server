package com.example.myreviewserver.application.auth.naver;

import com.example.myreviewserver.application.auth.AuthTokenResult;
import com.example.myreviewserver.application.auth.SocialLoginCommand;
import com.example.myreviewserver.application.auth.SocialLoginUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NaverLoginUseCaseTest {

	@Mock
	NaverOAuthClient naverOAuthClient;

	@Mock
	SocialLoginUseCase socialLoginUseCase;

	@InjectMocks
	NaverLoginUseCase naverLoginUseCase;

	@Test
	void exchangesCodeThenDelegatesToSocialLogin() {
		when(naverOAuthClient.fetchUserProfile("code-1", "state-1"))
			.thenReturn(new NaverUserProfile("naver-99", "a@test.com", "alice"));
		when(socialLoginUseCase.execute(any())).thenReturn(
			new AuthTokenResult("jwt", "Bearer", 1000L, 1L, "alice", true)
		);

		AuthTokenResult result = naverLoginUseCase.execute("code-1", "state-1");

		assertThat(result.accessToken()).isEqualTo("jwt");
		assertThat(result.newlyRegistered()).isTrue();
		verify(socialLoginUseCase).execute(eq(
			new SocialLoginCommand(AuthProvider.NAVER, "naver-99", "a@test.com", "alice")
		));
	}

	@Test
	void rejectsMissingCode() {
		assertThatThrownBy(() -> naverLoginUseCase.execute(" ", "state"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("authorization code");
	}
}
