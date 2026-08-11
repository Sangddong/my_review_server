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
class ListPlatformsUseCaseTest {

	@Autowired
	ListPlatformsUseCase listPlatformsUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void returnsOnlyOwnActivePlatformsInSortOrder() {
		User owner = userRepository.save(User.create("owner@test.com", "owner"));
		User other = userRepository.save(User.create("other@test.com", "other"));

		Platform second = platformRepository.save(
			Platform.create(owner.getId(), "유튜브", "#f8dac6", 1)
		);
		Platform first = platformRepository.save(
			Platform.create(owner.getId(), "블로그", "#c6f8c8", 0)
		);
		Platform deleted = platformRepository.save(
			Platform.create(owner.getId(), "숨김", "#dddddd", 2)
		);
		deleted.softDelete();
		platformRepository.save(deleted);
		platformRepository.save(Platform.create(other.getId(), "남의것", "#c6e4f8", 0));

		List<Platform> result = listPlatformsUseCase.execute(owner.getId());

		assertThat(result).extracting(Platform::getId).containsExactly(first.getId(), second.getId());
		assertThat(result).allMatch(Platform::isActive);
	}
}
