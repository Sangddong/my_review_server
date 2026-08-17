package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
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
class UpdateExperienceUseCaseTest {

	@Autowired
	UpdateExperienceUseCase updateExperienceUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void updatesFieldsAndReplacesPlatformsWhileKeepingRegistration() {
		User user = userRepository.save(User.create("exp-update@test.com", "owner"));
		Platform blog = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Platform insta = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 1));
		Platform youtube = platformRepository.save(Platform.create(user.getId(), "유튜브", "#333333", 2));

		Experience saved = experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(ExperiencePlatform.of(blog.getId(), true), ExperiencePlatform.of(insta.getId(), false))
		));
		saved.setPlatformRegistered(blog.getId(), true);
		saved = experienceRepository.save(saved);

		Experience updated = updateExperienceUseCase.update(
			user.getId(),
			saved.getId(),
			"성수 디저트",
			ExperienceType.DELIVERY,
			LocalDate.of(2026, 8, 21),
			null,
			LocalDate.of(2026, 8, 30),
			null,
			List.of(
				new UpdateExperienceUseCase.PlatformLink(blog.getId(), true),
				new UpdateExperienceUseCase.PlatformLink(youtube.getId(), false)
			)
		);

		assertThat(updated.getName()).isEqualTo("성수 디저트");
		assertThat(updated.getExperienceType()).isEqualTo(ExperienceType.DELIVERY);
		assertThat(updated.getReservationDate()).isEqualTo(LocalDate.of(2026, 8, 21));
		assertThat(updated.getReservationTime()).isEqualTo(LocalTime.of(14, 0));
		assertThat(updated.getReviewDeadline()).isEqualTo(LocalDate.of(2026, 8, 30));
		assertThat(updated.getDetailLink()).isEqualTo("https://example.com");
		assertThat(updated.isReviewSubmitted()).isFalse();
		assertThat(updated.getPlatformList()).extracting(ExperiencePlatform::getPlatformId)
			.containsExactly(blog.getId(), youtube.getId());
		assertThat(updated.getPlatformList()).extracting(ExperiencePlatform::isRegistered)
			.containsExactly(true, false);
	}

	@Test
	void rejectsMissingOtherUsersAndUnownedPlatform() {
		User owner = userRepository.save(User.create("exp-update-owner@test.com", "owner"));
		User other = userRepository.save(User.create("exp-update-other@test.com", "other"));
		Platform own = platformRepository.save(Platform.create(owner.getId(), "블로그", "#111111", 0));
		Platform foreign = platformRepository.save(Platform.create(other.getId(), "남의플", "#333333", 0));
		Experience saved = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(own.getId(), true))
		));

		assertThatThrownBy(() -> updateExperienceUseCase.update(
			other.getId(), saved.getId(), "이름", null, null, null, null, null, null
		)).isInstanceOf(DomainException.class).hasMessage("Experience not found");

		assertThatThrownBy(() -> updateExperienceUseCase.update(
			owner.getId(), 999_999L, "이름", null, null, null, null, null, null
		)).isInstanceOf(DomainException.class).hasMessage("Experience not found");

		assertThatThrownBy(() -> updateExperienceUseCase.update(
			owner.getId(),
			saved.getId(),
			null,
			null,
			null,
			null,
			null,
			null,
			List.of(new UpdateExperienceUseCase.PlatformLink(foreign.getId(), true))
		)).isInstanceOf(DomainException.class).hasMessage("Platform not found");
	}
}
