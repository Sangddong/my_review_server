package com.example.myreviewserver.adapter.outbound.persistence.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
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

		created.softDelete();
		platformRepository.save(created);

		assertThat(platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(1L)).isEmpty();
		assertThat(platformRepository.findActiveByIdAndUserId(created.getId(), 1L)).isEmpty();
		assertThat(platformRepository.findById(created.getId())).isPresent();
	}
}
