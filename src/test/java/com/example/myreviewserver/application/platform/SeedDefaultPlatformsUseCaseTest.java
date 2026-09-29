package com.example.myreviewserver.application.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SeedDefaultPlatformsUseCaseTest {

	@Autowired
	SeedDefaultPlatformsUseCase seedDefaultPlatformsUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void seedsSevenDefaultsOnceAndIsIdempotent() {
		User user = userRepository.save(User.create("seed@test.com", "seeder"));

		seedDefaultPlatformsUseCase.execute(user.getId());
		List<Platform> first = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(user.getId());
		assertThat(first).hasSize(7);
		assertThat(first).extracting(Platform::getName)
			.containsExactlyElementsOf(SeedDefaultPlatformsUseCase.defaultNames());
		assertThat(first).extracting(Platform::getSortOrder)
			.containsExactly(0, 1, 2, 3, 4, 5, 6);

		seedDefaultPlatformsUseCase.execute(user.getId());
		List<Platform> second = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(user.getId());
		assertThat(second).hasSize(7);
		assertThat(second).extracting(Platform::getId)
			.containsExactlyElementsOf(first.stream().map(Platform::getId).toList());
	}
}
