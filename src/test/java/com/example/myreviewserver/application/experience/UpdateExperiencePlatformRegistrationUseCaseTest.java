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
class UpdateExperiencePlatformRegistrationUseCaseTest {

	@Autowired
	UpdateExperiencePlatformRegistrationUseCase updateExperiencePlatformRegistrationUseCase;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void togglesRegistrationAndRejectsUnlinkedOrOtherUser() {
		User owner = userRepository.save(User.create("exp-reg@test.com", "owner"));
		User other = userRepository.save(User.create("exp-reg-other@test.com", "other"));
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

		Experience registered = updateExperiencePlatformRegistrationUseCase.update(
			owner.getId(), saved.getId(), 10L, true
		);
		assertThat(registered.isRequiredItemsComplete()).isTrue();
		assertThat(registered.getPlatformList()).filteredOn(p -> p.getPlatformId().equals(10L))
			.extracting(ExperiencePlatform::isRegistered)
			.containsExactly(true);

		Experience unregistered = updateExperiencePlatformRegistrationUseCase.update(
			owner.getId(), saved.getId(), 10L, false
		);
		assertThat(unregistered.isRequiredItemsComplete()).isFalse();
		assertThat(unregistered.getPlatformList()).filteredOn(p -> p.getPlatformId().equals(10L))
			.extracting(ExperiencePlatform::isRegistered)
			.containsExactly(false);

		assertThatThrownBy(() -> updateExperiencePlatformRegistrationUseCase.update(
			owner.getId(), saved.getId(), 99L, true
		)).isInstanceOf(DomainException.class).hasMessage("Platform is not linked to this experience");

		assertThatThrownBy(() -> updateExperiencePlatformRegistrationUseCase.update(
			other.getId(), saved.getId(), 10L, true
		)).isInstanceOf(DomainException.class).hasMessage("Experience not found");

		assertThatThrownBy(() -> updateExperiencePlatformRegistrationUseCase.update(
			owner.getId(), saved.getId(), 10L, null
		)).isInstanceOf(DomainException.class).hasMessage("registered is required");
	}
}
