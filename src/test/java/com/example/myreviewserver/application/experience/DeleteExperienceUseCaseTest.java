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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DeleteExperienceUseCaseTest {

	@Autowired
	DeleteExperienceUseCase deleteExperienceUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void hardDeletesOwnExperiencesAndRejectsMissingOrOtherUser() {
		User owner = userRepository.save(User.create("exp-delete@test.com", "owner"));
		User other = userRepository.save(User.create("exp-delete-other@test.com", "other"));
		Experience first = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(10L, true), ExperiencePlatform.of(20L, false))
		));
		first.setPlatformRegistered(10L, true);
		Experience persistedFirst = experienceRepository.save(first);
		Experience second = experienceRepository.save(Experience.create(
			owner.getId(),
			"홍대 맛집",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 28),
			null,
			List.of(ExperiencePlatform.of(11L, true))
		));
		Experience otherOwned = experienceRepository.save(Experience.create(
			other.getId(),
			"남의것",
			ExperienceType.DELIVERY,
			null,
			null,
			LocalDate.of(2026, 8, 30),
			null,
			List.of(ExperiencePlatform.of(30L, true))
		));

		deleteExperienceUseCase.delete(owner.getId(), List.of(persistedFirst.getId(), second.getId()));

		assertThat(experienceRepository.findById(persistedFirst.getId())).isEmpty();
		assertThat(experienceRepository.findById(second.getId())).isEmpty();
		assertThat(experienceRepository.findByIdAndUserId(persistedFirst.getId(), owner.getId())).isEmpty();

		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), List.of(persistedFirst.getId())))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), List.of(999_999L)))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), List.of(otherOwned.getId())))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThat(experienceRepository.findById(otherOwned.getId())).isPresent();
	}
}
