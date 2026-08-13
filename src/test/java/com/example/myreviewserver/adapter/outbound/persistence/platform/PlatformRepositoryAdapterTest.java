package com.example.myreviewserver.adapter.outbound.persistence.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Persistence adapter test against H2 with schema created from entities.
 *
 * @DataJpaTest: loads only JPA-related Spring config (lighter than full @SpringBootTest).
 * @Import: includes our adapter (not auto-scanned by DataJpaTest by default).
 */
@DataJpaTest
@Import(PlatformRepositoryAdapter.class)
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.flyway.enabled=false",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PlatformRepositoryAdapterTest {

	@Autowired
	PlatformRepository platformRepository;

	@Test
	void savesListsAndSoftDeletes() {
		Platform created = platformRepository.save(
			Platform.create(1L, "블로그", "#c6f8c8", 0)
		);

		assertThat(created.getId()).isNotNull();
		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(1L)).hasSize(1);

		assertThat(platformRepository.softDeleteActiveByIdAndUserId(created.getId(), 1L)).isTrue();
		assertThat(platformRepository.softDeleteActiveByIdAndUserId(created.getId(), 1L)).isFalse();

		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(1L)).isEmpty();
		assertThat(platformRepository.findActiveByIdAndUserId(created.getId(), 1L)).isEmpty();
		assertThat(platformRepository.findById(created.getId())).isPresent();

		Platform updated = platformRepository.updateActiveByIdAndUserId(
			created.getId(),
			1L,
			"브런치",
			"#112233"
		).orElse(null);
		assertThat(updated).isNull();

		Platform second = platformRepository.save(Platform.create(1L, "유튜브", "#f8dac6", 1));
		assertThat(platformRepository.existsActiveByUserIdAndNameExcludingId(1L, "유튜브", created.getId()))
			.isTrue();
		Platform renamed = platformRepository.updateActiveByIdAndUserId(second.getId(), 1L, "브런치", null)
			.orElseThrow();
		assertThat(renamed.getName()).isEqualTo("브런치");
		assertThat(renamed.getColor()).isEqualTo("#f8dac6");

		Platform a = platformRepository.save(Platform.create(1L, "A", "#aaaaaa", 0));
		Platform b = platformRepository.save(Platform.create(1L, "B", "#bbbbbb", 1));
		assertThat(platformRepository.reorderActiveByUserId(1L, List.of(b.getId(), a.getId(), second.getId())))
			.isTrue();
		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(1L))
			.extracting(Platform::getId)
			.containsExactly(b.getId(), a.getId(), second.getId());
	}
}
