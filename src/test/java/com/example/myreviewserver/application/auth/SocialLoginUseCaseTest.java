package com.example.myreviewserver.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.user.AuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SocialLoginUseCaseTest {

	@Autowired
	SocialLoginUseCase socialLoginUseCase;

	@Test
	void registersThenLogsInExistingUser() {
		AuthTokenResult first = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.GOOGLE, "google-1", "a@test.com", "alice")
		);
		assertThat(first.newlyRegistered()).isTrue();
		assertThat(first.accessToken()).isNotBlank();
		assertThat(first.userId()).isNotNull();

		AuthTokenResult second = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.GOOGLE, "google-1", "a@test.com", "alice")
		);
		assertThat(second.newlyRegistered()).isFalse();
		assertThat(second.userId()).isEqualTo(first.userId());
	}
}
