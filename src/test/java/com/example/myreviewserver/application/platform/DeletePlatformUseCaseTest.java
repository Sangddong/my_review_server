package com.example.myreviewserver.application.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DeletePlatformUseCaseTest {

	@Autowired
	DeletePlatformUseCase deletePlatformUseCase;

	@Autowired
	CreatePlatformUseCase createPlatformUseCase;

	@Autowired
	ListPlatformsUseCase listPlatformsUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void softDeletesKeepsRowAndRejectsMissingDeletedAndOtherUser() {
		User user = userRepository.save(User.create("delete@test.com", "deleter"));
		User other = userRepository.save(User.create("delete-other@test.com", "other"));

		Platform blog = createPlatformUseCase.execute(user.getId(), "블로그", "#c6f8c8");
		Platform youtube = createPlatformUseCase.execute(user.getId(), "유튜브", "#f8dac6");
		Platform otherOwned = createPlatformUseCase.execute(other.getId(), "남의것", "#111111");

		deletePlatformUseCase.execute(user.getId(), blog.getId());

		Platform deleted = platformRepository.findById(blog.getId()).orElseThrow();
		assertThat(deleted.isActive()).isFalse();
		assertThat(deleted.getIsDeleted()).isEqualTo(1);
		assertThat(deleted.getDeletedAt()).isNotNull();
		assertThat(listPlatformsUseCase.execute(user.getId()))
			.extracting(Platform::getId)
			.containsExactly(youtube.getId());

		assertThatThrownBy(() -> deletePlatformUseCase.execute(user.getId(), blog.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");

		assertThatThrownBy(() -> deletePlatformUseCase.execute(user.getId(), 999_999L))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");

		assertThatThrownBy(() -> deletePlatformUseCase.execute(user.getId(), otherOwned.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");
		assertThat(platformRepository.findById(otherOwned.getId()).orElseThrow().isActive()).isTrue();
	}
}
