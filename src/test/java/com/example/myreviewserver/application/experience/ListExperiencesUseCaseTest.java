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
class ListExperiencesUseCaseTest {

	@Autowired
	ListExperiencesUseCase listExperiencesUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void listsOwnUpcomingAndCompletedInReservationOrder() {
		User owner = userRepository.save(User.create("exp-list@test.com", "owner"));
		User other = userRepository.save(User.create("exp-list-other@test.com", "other"));

		Experience laterDate = saveUpcoming(owner.getId(), "나중", LocalDate.of(2026, 8, 21), LocalTime.of(10, 0));
		Experience earlierDate = saveUpcoming(owner.getId(), "먼저", LocalDate.of(2026, 8, 20), LocalTime.of(18, 0));
		Experience sameDateEarlierTime = saveUpcoming(owner.getId(), "같은날아침", LocalDate.of(2026, 8, 20), LocalTime.of(9, 0));
		Experience noReservation = saveUpcoming(owner.getId(), "날짜없음", null, null);
		Experience otherOwned = saveUpcoming(other.getId(), "남의것", LocalDate.of(2026, 8, 19), LocalTime.of(9, 0));

		Experience completed = saveUpcoming(owner.getId(), "완료", LocalDate.of(2026, 8, 10), LocalTime.of(12, 0));
		completed.submitReview();
		experienceRepository.save(completed);

		List<Experience> upcoming = listExperiencesUseCase.execute(owner.getId(), ExperienceListStatus.upcoming);
		assertThat(upcoming).extracting(Experience::getName)
			.containsExactly("같은날아침", "먼저", "나중", "날짜없음");
		assertThat(upcoming).extracting(Experience::getId)
			.containsExactly(
				sameDateEarlierTime.getId(),
				earlierDate.getId(),
				laterDate.getId(),
				noReservation.getId()
			)
			.doesNotContain(otherOwned.getId(), completed.getId());

		List<Experience> done = listExperiencesUseCase.execute(owner.getId(), ExperienceListStatus.completed);
		assertThat(done).extracting(Experience::getId).containsExactly(completed.getId());

		assertThatThrownBy(() -> ExperienceListStatus.from("all"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("upcoming or completed");
	}

	private Experience saveUpcoming(Long userId, String name, LocalDate date, LocalTime time) {
		return experienceRepository.save(Experience.create(
			userId,
			name,
			ExperienceType.VISIT,
			date,
			time,
			LocalDate.of(2026, 9, 1),
			null,
			List.of(ExperiencePlatform.of(10L, true))
		));
	}
}
