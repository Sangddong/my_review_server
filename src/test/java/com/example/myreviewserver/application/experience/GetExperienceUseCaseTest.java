package com.example.myreviewserver.application.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
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
class GetExperienceUseCaseTest {

	@Autowired
	GetExperienceUseCase getExperienceUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void returnsOwnExperienceWithPlatforms() {
		User owner = userRepository.save(User.create("exp-get@test.com", "owner"));
		Experience saved = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(ExperiencePlatform.of(10L, true), ExperiencePlatform.of(20L, false))
		));
		saved.setPlatformRegistered(10L, true);
		saved = experienceRepository.save(saved);

		Experience found = getExperienceUseCase.get(owner.getId(), saved.getId());

		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getName()).isEqualTo("성수 카페");
		assertThat(found.isReviewSubmitted()).isFalse();
		assertThat(found.isRequiredItemsComplete()).isTrue();
		assertThat(found.getPlatforms()).extracting(ExperiencePlatform::getPlatformId)
			.containsExactly(10L, 20L);
		assertThat(found.getPlatforms()).extracting(ExperiencePlatform::isRequired)
			.containsExactly(true, false);
		assertThat(found.getPlatforms()).extracting(ExperiencePlatform::isRegistered)
			.containsExactly(true, false);
	}

	@Test
	void rejectsMissingOrOtherUsersExperience() {
		User owner = userRepository.save(User.create("exp-get-owner@test.com", "owner"));
		User other = userRepository.save(User.create("exp-get-other@test.com", "other"));
		Experience saved = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(10L, true))
		));

		assertThatThrownBy(() -> getExperienceUseCase.get(other.getId(), saved.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> getExperienceUseCase.get(owner.getId(), 999_999L))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
	}
}
