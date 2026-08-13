package com.example.myreviewserver.domain.experience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExperienceTest {

	@Test
	void createDerivesRequiredCompleteAndTracksRegistrationAndSubmission() {
		Experience experience = Experience.create(
			1L,
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(
				ExperiencePlatform.of(10L, true),
				ExperiencePlatform.of(20L, false)
			)
		);

		assertThat(experience.isReviewSubmitted()).isFalse();
		assertThat(experience.isRequiredItemsComplete()).isFalse();

		experience.setPlatformRegistered(10L, true);
		assertThat(experience.isRequiredItemsComplete()).isTrue();

		experience.submitReview();
		assertThat(experience.isReviewSubmitted()).isTrue();
		assertThat(experience.getIsReviewSubmitted()).isEqualTo(1);

		experience.unsubmitReview();
		assertThat(experience.isReviewSubmitted()).isFalse();
	}

	@Test
	void rejectsInvalidCreateAndUnknownPlatformRegistration() {
		assertThatThrownBy(() -> Experience.create(
			1L,
			"체험",
			ExperienceType.DELIVERY,
			null,
			null,
			null,
			null,
			List.of(ExperiencePlatform.of(10L, true))
		)).isInstanceOf(DomainException.class).hasMessageContaining("reviewDeadline");

		assertThatThrownBy(() -> Experience.create(
			1L,
			"체험",
			ExperienceType.PRESS,
			null,
			null,
			LocalDate.of(2026, 9, 1),
			null,
			List.of()
		)).isInstanceOf(DomainException.class).hasMessageContaining("at least one platform");

		assertThatThrownBy(() -> Experience.create(
			1L,
			"체험",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 9, 1),
			null,
			List.of(ExperiencePlatform.of(10L, true), ExperiencePlatform.of(10L, false))
		)).isInstanceOf(DomainException.class).hasMessageContaining("duplicate");

		Experience experience = Experience.create(
			1L,
			"체험",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 9, 1),
			null,
			List.of(ExperiencePlatform.of(10L, true))
		);
		assertThatThrownBy(() -> experience.setPlatformRegistered(99L, true))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not linked");
	}
}
