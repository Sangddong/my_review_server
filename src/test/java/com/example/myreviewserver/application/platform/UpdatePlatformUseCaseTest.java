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
class UpdatePlatformUseCaseTest {

	@Autowired
	UpdatePlatformUseCase updatePlatformUseCase;

	@Autowired
	CreatePlatformUseCase createPlatformUseCase;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void updatesNameAndColorRejectsDuplicateDeletedAndMissing() {
		User user = userRepository.save(User.create("update@test.com", "updater"));
		User other = userRepository.save(User.create("update-other@test.com", "other"));

		Platform blog = createPlatformUseCase.execute(user.getId(), "블로그", "#c6f8c8");
		Platform youtube = createPlatformUseCase.execute(user.getId(), "유튜브", "#f8dac6");
		Platform otherOwned = createPlatformUseCase.execute(other.getId(), "남의것", "#111111");

		Platform renamed = updatePlatformUseCase.execute(user.getId(), blog.getId(), "브런치", null);
		assertThat(renamed.getName()).isEqualTo("브런치");
		assertThat(renamed.getColor()).isEqualTo("#c6f8c8");

		Platform recolored = updatePlatformUseCase.execute(user.getId(), blog.getId(), null, "#112233");
		assertThat(recolored.getName()).isEqualTo("브런치");
		assertThat(recolored.getColor()).isEqualTo("#112233");

		Platform sameName = updatePlatformUseCase.execute(user.getId(), blog.getId(), "브런치", "#aabbcc");
		assertThat(sameName.getName()).isEqualTo("브런치");
		assertThat(sameName.getColor()).isEqualTo("#aabbcc");

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), youtube.getId(), "브런치", null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("already exists");

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), blog.getId(), null, null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("name or color");

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), 999_999L, "새이름", null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), otherOwned.getId(), "가로채기", null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");

		youtube.softDelete();
		platformRepository.save(youtube);

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), youtube.getId(), "삭제됨수정", null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");

		assertThatThrownBy(() -> updatePlatformUseCase.execute(user.getId(), youtube.getId(), "브런치", null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");
	}
}
