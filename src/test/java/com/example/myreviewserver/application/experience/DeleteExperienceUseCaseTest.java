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
	void hardDeletesOwnExperienceAndRejectsMissingOrOtherUser() {
		User owner = userRepository.save(User.create("exp-delete@test.com", "owner"));
		User other = userRepository.save(User.create("exp-delete-other@test.com", "other"));
		Experience saved = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(10L, true), ExperiencePlatform.of(20L, false))
		));
		saved.setPlatformRegistered(10L, true);
		Experience persisted = experienceRepository.save(saved);
		Long experienceId = persisted.getId();
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

		deleteExperienceUseCase.delete(owner.getId(), experienceId);

		assertThat(experienceRepository.findById(experienceId)).isEmpty();
		assertThat(experienceRepository.findByIdAndUserId(experienceId, owner.getId())).isEmpty();

		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), experienceId))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), 999_999L))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> deleteExperienceUseCase.delete(owner.getId(), otherOwned.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThat(experienceRepository.findById(otherOwned.getId())).isPresent();
	}
}
