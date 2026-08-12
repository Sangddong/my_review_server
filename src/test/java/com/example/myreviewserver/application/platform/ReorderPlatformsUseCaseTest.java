package com.example.myreviewserver.application.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReorderPlatformsUseCaseTest {

	@Autowired
	ReorderPlatformsUseCase reorderPlatformsUseCase;

	@Autowired
	CreatePlatformUseCase createPlatformUseCase;

	@Autowired
	DeletePlatformUseCase deletePlatformUseCase;

	@Autowired
	ListPlatformsUseCase listPlatformsUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void reordersActiveOnlyAndRejectsInvalidOrderedIds() {
		User user = userRepository.save(User.create("reorder@test.com", "reord"));
		User other = userRepository.save(User.create("reorder-other@test.com", "other"));

		Platform first = createPlatformUseCase.execute(user.getId(), "블로그", "#c6f8c8");
		Platform second = createPlatformUseCase.execute(user.getId(), "유튜브", "#f8dac6");
		Platform third = createPlatformUseCase.execute(user.getId(), "인스타", "#dac6f8");
		Platform otherOwned = createPlatformUseCase.execute(other.getId(), "남의것", "#111111");

		List<Platform> reordered = reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(third.getId(), first.getId(), second.getId())
		);
		assertThat(reordered).extracting(Platform::getId)
			.containsExactly(third.getId(), first.getId(), second.getId());
		assertThat(reordered).extracting(Platform::getSortOrder)
			.containsExactly(0, 1, 2);
		assertThat(listPlatformsUseCase.execute(user.getId())).extracting(Platform::getId)
			.containsExactly(third.getId(), first.getId(), second.getId());

		deletePlatformUseCase.execute(user.getId(), second.getId());
		assertThat(platformRepository.findById(second.getId()).orElseThrow().isActive()).isFalse();

		assertThatThrownBy(() -> reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(third.getId(), first.getId(), second.getId())
		))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("orderedIds must match active platforms exactly");

		assertThatThrownBy(() -> reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(third.getId())
		))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("orderedIds must match active platforms exactly");

		assertThatThrownBy(() -> reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(third.getId(), first.getId(), otherOwned.getId())
		))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("orderedIds must match active platforms exactly");

		assertThatThrownBy(() -> reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(third.getId(), first.getId(), first.getId())
		))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("duplicates");

		List<Platform> afterDelete = reorderPlatformsUseCase.execute(
			user.getId(),
			List.of(first.getId(), third.getId())
		);
		assertThat(afterDelete).extracting(Platform::getId)
			.containsExactly(first.getId(), third.getId());
	}
}
