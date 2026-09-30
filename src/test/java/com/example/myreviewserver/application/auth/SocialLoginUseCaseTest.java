package com.example.myreviewserver.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.application.platform.SeedDefaultPlatformsUseCase;
import com.example.myreviewserver.application.user.SocialAccountUnlinkClient;
import com.example.myreviewserver.application.user.WithdrawUserUseCase;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SocialLoginUseCaseTest {

	@Autowired
	SocialLoginUseCase socialLoginUseCase;

	@Autowired
	WithdrawUserUseCase withdrawUserUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@MockitoBean
	SocialAccountUnlinkClient socialAccountUnlinkClient;

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

	@Test
	void withdrawThenSameSocialRegistersFreshUserWithDefaultPlatforms() {
		AuthTokenResult first = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.NAVER, "naver-rereg-1", "rereg@test.com", "rereg")
		);
		assertThat(first.newlyRegistered()).isTrue();
		Long withdrawnUserId = first.userId();
		List<Long> oldPlatformIds = platformRepository
			.findActiveByUserIdOrderBySortOrderAscIdAsc(withdrawnUserId)
			.stream()
			.map(Platform::getId)
			.toList();
		assertThat(oldPlatformIds).hasSize(7);

		withdrawUserUseCase.execute(withdrawnUserId);
		assertThat(userRepository.findById(withdrawnUserId).orElseThrow().getIsDeleted()).isEqualTo(1);
		assertThat(userRepository.findByProvider(AuthProvider.NAVER, "naver-rereg-1")).isEmpty();

		AuthTokenResult again = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.NAVER, "naver-rereg-1", "rereg@test.com", "rereg")
		);
		assertThat(again.newlyRegistered()).isTrue();
		assertThat(again.userId()).isNotEqualTo(withdrawnUserId);

		List<Platform> newPlatforms = platformRepository
			.findActiveByUserIdOrderBySortOrderAscIdAsc(again.userId());
		assertThat(newPlatforms).extracting(Platform::getName)
			.containsExactlyElementsOf(SeedDefaultPlatformsUseCase.defaultNames());
		assertThat(newPlatforms).extracting(Platform::getId)
			.doesNotContainAnyElementsOf(oldPlatformIds);

		// Soft-deleted account is not revived
		assertThat(userRepository.findById(withdrawnUserId).orElseThrow().getIsDeleted()).isEqualTo(1);
	}

	@Test
	void orphanOauthOnSoftDeletedUserAllowsReregister() {
		User deleted = userRepository.save(User.create("orphan@test.com", "orphan"));
		deleted.withdraw(Instant.now());
		deleted = userRepository.save(deleted);
		userRepository.saveOauthAccount(deleted.getId(), AuthProvider.KAKAO, "kakao-orphan-1", null);

		AuthTokenResult result = socialLoginUseCase.execute(
			new SocialLoginCommand(AuthProvider.KAKAO, "kakao-orphan-1", "orphan@test.com", "orphan")
		);
		assertThat(result.newlyRegistered()).isTrue();
		assertThat(result.userId()).isNotEqualTo(deleted.getId());
		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(result.userId()))
			.hasSize(7);
		assertThat(userRepository.findById(deleted.getId()).orElseThrow().getIsDeleted()).isEqualTo(1);
	}

	@Test
	void keepsStoredRefreshTokenWhenOnlyAccessTokenReturned() {
		AuthTokenResult first = socialLoginUseCase.execute(
			new SocialLoginCommand(
				AuthProvider.GOOGLE, "google-keep-rt", "keep@test.com", "keep", "refresh-long", null
			)
		);
		assertThat(first.newlyRegistered()).isTrue();
		assertThat(userRepository.findOauthAccountsByUserId(first.userId()))
			.extracting(link -> link.refreshToken())
			.containsExactly("refresh-long");

		socialLoginUseCase.execute(
			new SocialLoginCommand(
				AuthProvider.GOOGLE, "google-keep-rt", "keep@test.com", "keep", null, "access-short"
			)
		);

		assertThat(userRepository.findOauthAccountsByUserId(first.userId()))
			.extracting(link -> link.refreshToken())
			.containsExactly("refresh-long");
	}

	@Test
	void storesAccessTokenWhenRefreshMissingOnRegister() {
		AuthTokenResult first = socialLoginUseCase.execute(
			new SocialLoginCommand(
				AuthProvider.GOOGLE, "google-access-only", "ao@test.com", "ao", null, "access-1"
			)
		);
		assertThat(first.newlyRegistered()).isTrue();
		assertThat(userRepository.findOauthAccountsByUserId(first.userId()))
			.extracting(link -> link.refreshToken())
			.containsExactly("access-1");
	}
}
