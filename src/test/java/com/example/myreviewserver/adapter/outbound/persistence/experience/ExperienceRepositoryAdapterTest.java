package com.example.myreviewserver.adapter.outbound.persistence.experience;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Persistence adapter test against H2 with schema created from entities.
 *
 * @DataJpaTest: JPA 관련 설정만 로드.
 * @Import: DataJpaTest가 스캔하지 않는 어댑터를 포함.
 */
@DataJpaTest
@Import(ExperienceRepositoryAdapter.class)
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.flyway.enabled=false",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ExperienceRepositoryAdapterTest {

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void savesLoadsListsUpdatesAndDeletes() {
		Experience created = experienceRepository.save(Experience.create(
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
		));

		assertThat(created.getId()).isNotNull();
		assertThat(created.getPlatforms()).hasSize(2);
		assertThat(created.isRequiredItemsComplete()).isFalse();
		assertThat(experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(1L)).hasSize(1);
		assertThat(experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(1L)).isEmpty();

		created.setPlatformRegistered(10L, true);
		created.submitReview();
		Experience updated = experienceRepository.save(created);

		assertThat(updated.isRequiredItemsComplete()).isTrue();
		assertThat(updated.isReviewSubmitted()).isTrue();
		assertThat(updated.getPlatforms())
			.filteredOn(ExperiencePlatform::isRegistered)
			.extracting(ExperiencePlatform::getPlatformId)
			.containsExactly(10L);
		assertThat(experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(1L)).isEmpty();
		assertThat(experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(1L)).hasSize(1);

		Experience later = experienceRepository.save(Experience.create(
			1L,
			"배달 체험",
			ExperienceType.DELIVERY,
			null,
			null,
			LocalDate.of(2026, 8, 30),
			null,
			List.of(ExperiencePlatform.of(30L, true))
		));
		assertThat(experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(1L))
			.extracting(Experience::getId)
			.containsExactly(later.getId());

		assertThat(experienceRepository.deleteByIdAndUserId(updated.getId(), 1L)).isTrue();
		assertThat(experienceRepository.findByIdAndUserId(updated.getId(), 1L)).isEmpty();
		assertThat(experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(1L)).isEmpty();
		assertThat(experienceRepository.deleteByIdAndUserId(updated.getId(), 1L)).isFalse();
	}
}
