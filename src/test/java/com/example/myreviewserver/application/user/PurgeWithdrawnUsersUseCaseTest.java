package com.example.myreviewserver.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PurgeWithdrawnUsersUseCaseTest {

	@Autowired
	PurgeWithdrawnUsersUseCase purgeWithdrawnUsersUseCase;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	DeviceTokenRepository deviceTokenRepository;

	@Test
	void hardDeletesExpiredWithdrawnUsersAndKeepsRecentAndActive() {
		User expired = userRepository.save(User.create("purge-expired@test.com", "expired"));
		User recent = userRepository.save(User.create("purge-recent@test.com", "recent"));
		User active = userRepository.save(User.create("purge-active@test.com", "active"));

		Platform expiredPlatform = platformRepository.save(Platform.create(expired.getId(), "블로그", "#111111", 0));
		Experience expiredExperience = experienceRepository.save(Experience.create(
			expired.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(expiredPlatform.getId(), true))
		));
		expiredExperience.setPlatformRegistered(expiredPlatform.getId(), true);
		expiredExperience = experienceRepository.save(expiredExperience);
		deviceTokenRepository.save(DeviceToken.create(expired.getId(), "expired-token", DevicePlatform.IOS));
		userRepository.saveOauthAccount(expired.getId(), AuthProvider.NAVER, "naver-expired");

		platformRepository.save(Platform.create(recent.getId(), "인스타", "#222222", 0));
		platformRepository.save(Platform.create(active.getId(), "유튜브", "#333333", 0));

		expired.withdraw(OffsetDateTime.now(ZoneOffset.UTC).minusMonths(7).toInstant());
		userRepository.save(expired);
		recent.withdraw(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toInstant());
		userRepository.save(recent);

		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6).toInstant();
		int deleted = purgeWithdrawnUsersUseCase.execute(cutoff);

		assertThat(deleted).isEqualTo(1);
		assertThat(userRepository.findById(expired.getId())).isEmpty();
		assertThat(userRepository.findByProvider(AuthProvider.NAVER, "naver-expired")).isEmpty();
		assertThat(experienceRepository.findById(expiredExperience.getId())).isEmpty();
		assertThat(platformRepository.findById(expiredPlatform.getId())).isEmpty();
		assertThat(deviceTokenRepository.findByToken("expired-token")).isEmpty();

		assertThat(userRepository.findById(recent.getId())).isPresent();
		assertThat(userRepository.findById(active.getId())).isPresent();
		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(recent.getId())).hasSize(1);
		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(active.getId())).hasSize(1);
	}
}
