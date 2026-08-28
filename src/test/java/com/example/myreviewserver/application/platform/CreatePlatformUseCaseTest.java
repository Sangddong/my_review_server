package com.example.myreviewserver.application.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CreatePlatformUseCaseTest {

	@Autowired
	CreatePlatformUseCase createPlatformUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void createsAtEndRejectsActiveDuplicateAndIgnoresDeletedName() {
		User user = userRepository.save(User.create("create@test.com", "creator"));

		Platform first = createPlatformUseCase.execute(user.getId(), "블로그", "#c6f8c8");
		Platform second = createPlatformUseCase.execute(user.getId(), "유튜브", "#f8dac6");
		assertThat(first.getSortOrder()).isEqualTo(0);
		assertThat(second.getSortOrder()).isEqualTo(1);

		assertThatThrownBy(() -> createPlatformUseCase.execute(user.getId(), "블로그", "#ffffff"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("already exists");

		first.softDelete(Instant.now());
		platformRepository.save(first);

		Platform recreated = createPlatformUseCase.execute(user.getId(), "블로그", "#112233");
		assertThat(recreated.getId()).isNotEqualTo(first.getId());
		assertThat(recreated.isActive()).isTrue();
		assertThat(recreated.getColor()).isEqualTo("#112233");
		assertThat(recreated.getSortOrder()).isEqualTo(1);
		assertThat(platformRepository.findById(first.getId()).orElseThrow().isActive()).isFalse();
	}
}
