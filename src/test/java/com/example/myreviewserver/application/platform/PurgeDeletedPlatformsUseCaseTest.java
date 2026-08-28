package com.example.myreviewserver.application.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.adapter.outbound.persistence.platform.PlatformJpaEntity;
import com.example.myreviewserver.adapter.outbound.persistence.platform.SpringDataPlatformRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
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
class PurgeDeletedPlatformsUseCaseTest {

	@Autowired
	PurgeDeletedPlatformsUseCase purgeDeletedPlatformsUseCase;

	@Autowired
	DeletePlatformUseCase deletePlatformUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	SpringDataPlatformRepository springDataPlatformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void hardDeletesExpiredUnlinkedPlatformsAndKeepsRecentLinkedAndActiveOnes() {
		User user = userRepository.save(User.create("platform-purge@test.com", "platformpurge"));

		Platform expired = platformRepository.save(Platform.create(user.getId(), "만료", "#111111", 0));
		Platform recent = platformRepository.save(Platform.create(user.getId(), "최근", "#222222", 1));
		Platform linked = platformRepository.save(Platform.create(user.getId(), "연결됨", "#333333", 2));
		Platform active = platformRepository.save(Platform.create(user.getId(), "활성", "#444444", 3));

		experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(linked.getId(), true))
		));

		deletePlatformUseCase.execute(user.getId(), expired.getId());
		deletePlatformUseCase.execute(user.getId(), recent.getId());
		deletePlatformUseCase.execute(user.getId(), linked.getId());

		Instant longAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6).toInstant();
		backdateDeletedAt(expired.getId(), longAgo);
		backdateDeletedAt(linked.getId(), longAgo);

		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(3).toInstant();
		purgeDeletedPlatformsUseCase.execute(cutoff);

		assertThat(springDataPlatformRepository.findById(expired.getId())).isEmpty();
		assertThat(springDataPlatformRepository.findById(recent.getId())).isPresent();
		assertThat(springDataPlatformRepository.findById(linked.getId())).isPresent();
		assertThat(platformRepository.findActiveByIdAndUserId(active.getId(), user.getId())).isPresent();
	}

	private void backdateDeletedAt(Long platformId, Instant deletedAt) {
		PlatformJpaEntity entity = springDataPlatformRepository.findById(platformId).orElseThrow();
		entity.setDeletedAt(deletedAt);
		springDataPlatformRepository.save(entity);
	}
}
