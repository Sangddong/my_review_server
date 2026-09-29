package com.example.myreviewserver.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.application.platform.SeedDefaultPlatformsUseCase;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.AuthProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SocialLoginUseCaseTest {

	@Autowired
	SocialLoginUseCase socialLoginUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Test
	void registersThenLogsInExistingUser() {
		AuthTokenResult first = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.GOOGLE, "google-1", "a@test.com", "alice")
		);
		assertThat(first.newlyRegistered()).isTrue();
		assertThat(first.accessToken()).isNotBlank();
		assertThat(first.userId()).isNotNull();

		List<Platform> seeded = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(first.userId());
		assertThat(seeded).extracting(Platform::getName)
			.containsExactlyElementsOf(SeedDefaultPlatformsUseCase.defaultNames());

		AuthTokenResult second = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.GOOGLE, "google-1", "a@test.com", "alice")
		);
		assertThat(second.newlyRegistered()).isFalse();
		assertThat(second.userId()).isEqualTo(first.userId());

		List<Platform> afterLogin = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(second.userId());
		assertThat(afterLogin).hasSize(7);
		assertThat(afterLogin).extracting(Platform::getId)
			.containsExactlyElementsOf(seeded.stream().map(Platform::getId).toList());
	}
}
