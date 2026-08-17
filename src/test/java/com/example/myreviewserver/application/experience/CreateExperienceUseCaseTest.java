package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CreateExperienceUseCaseTest {

	@Autowired
	CreateExperienceUseCase createExperienceUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void createsExperienceWithOwnedPlatforms() {
		User user = userRepository.save(User.create("exp-create@test.com", "owner"));
		Platform required = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Platform optional = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 1));

		Experience created = createExperienceUseCase.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(
				new CreateExperienceUseCase.PlatformLink(required.getId(), true),
				new CreateExperienceUseCase.PlatformLink(optional.getId(), false)
			)
		);

		assertThat(created.getId()).isNotNull();
		assertThat(created.getName()).isEqualTo("성수 카페");
		assertThat(created.isReviewSubmitted()).isFalse();
		assertThat(created.getPlatformList()).extracting(ExperiencePlatform::getPlatformId)
			.containsExactly(required.getId(), optional.getId());
		assertThat(created.getPlatformList()).extracting(ExperiencePlatform::isRequired)
			.containsExactly(true, false);
		assertThat(created.getPlatformList()).extracting(ExperiencePlatform::isRegistered)
			.containsExactly(false, false);
	}

	@Test
	void rejectsMissingDeadlineOtherUsersPlatformAndNoRequired() {
		User owner = userRepository.save(User.create("exp-create-owner@test.com", "owner"));
		User other = userRepository.save(User.create("exp-create-other@test.com", "other"));
		Platform own = platformRepository.save(Platform.create(owner.getId(), "블로그", "#111111", 0));
		Platform foreign = platformRepository.save(Platform.create(other.getId(), "남의플", "#333333", 0));

		assertThatThrownBy(() -> createExperienceUseCase.create(
			owner.getId(),
			"체험",
			ExperienceType.VISIT,
			null,
			null,
			null,
			null,
			List.of(new CreateExperienceUseCase.PlatformLink(own.getId(), true))
		)).isInstanceOf(DomainException.class).hasMessageContaining("reviewDeadline");

		assertThatThrownBy(() -> createExperienceUseCase.create(
			owner.getId(),
			"체험",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 9, 1),
			null,
			List.of(new CreateExperienceUseCase.PlatformLink(foreign.getId(), true))
		)).isInstanceOf(DomainException.class).hasMessage("Platform not found");

		assertThatThrownBy(() -> createExperienceUseCase.create(
			owner.getId(),
			"체험",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 9, 1),
			null,
			List.of(new CreateExperienceUseCase.PlatformLink(own.getId(), false))
		)).isInstanceOf(DomainException.class).hasMessageContaining("at least one required platform");
	}
}
