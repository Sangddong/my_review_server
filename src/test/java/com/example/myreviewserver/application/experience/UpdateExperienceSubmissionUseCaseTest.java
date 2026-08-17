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
class UpdateExperienceSubmissionUseCaseTest {

	@Autowired
	UpdateExperienceSubmissionUseCase updateExperienceSubmissionUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void togglesSubmissionAndMovesBetweenUpcomingAndCompleted() {
		User owner = userRepository.save(User.create("exp-submit@test.com", "owner"));
		User other = userRepository.save(User.create("exp-submit-other@test.com", "other"));
		Experience saved = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(10L, true))
		));

		Experience submitted = updateExperienceSubmissionUseCase.update(owner.getId(), saved.getId(), true);
		assertThat(submitted.isReviewSubmitted()).isTrue();
		assertThat(experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(owner.getId()))
			.extracting(Experience::getId)
			.doesNotContain(saved.getId());
		assertThat(experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(owner.getId()))
			.extracting(Experience::getId)
			.containsExactly(saved.getId());

		Experience unsubmitted = updateExperienceSubmissionUseCase.update(owner.getId(), saved.getId(), false);
		assertThat(unsubmitted.isReviewSubmitted()).isFalse();
		assertThat(experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(owner.getId()))
			.extracting(Experience::getId)
			.containsExactly(saved.getId());
		assertThat(experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(owner.getId()))
			.isEmpty();

		assertThatThrownBy(() -> updateExperienceSubmissionUseCase.update(other.getId(), saved.getId(), true))
			.isInstanceOf(DomainException.class)
			.hasMessage("Experience not found");
		assertThatThrownBy(() -> updateExperienceSubmissionUseCase.update(owner.getId(), saved.getId(), null))
			.isInstanceOf(DomainException.class)
			.hasMessage("submitted is required");
	}
}
