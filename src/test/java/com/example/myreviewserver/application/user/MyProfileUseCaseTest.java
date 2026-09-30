package com.example.myreviewserver.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
class MyProfileUseCaseTest {

	@Autowired
	GetMyProfileUseCase getMyProfileUseCase;

	@Autowired
	UpdateMyNicknameUseCase updateMyNicknameUseCase;

	@Autowired
	UserRepository userRepository;

	@Test
	void getsAndUpdatesNickname() {
		User user = userRepository.save(User.create("profile@test.com", "oldNick"));

		User loaded = getMyProfileUseCase.execute(user.getId());
		assertThat(loaded.getNickname()).isEqualTo("oldNick");
		assertThat(loaded.getEmail()).isEqualTo("profile@test.com");
		assertThat(loaded.getCreatedAt()).isNotNull();

		User updated = updateMyNicknameUseCase.execute(user.getId(), "  newNick  ");
		assertThat(updated.getNickname()).isEqualTo("newNick");
		assertThat(userRepository.findById(user.getId()).orElseThrow().getNickname()).isEqualTo("newNick");
	}

	@Test
	void rejectsBlankNicknameAndWithdrawnUser() {
		User user = userRepository.save(User.create("profile-bad@test.com", "nick"));

		assertThatThrownBy(() -> updateMyNicknameUseCase.execute(user.getId(), "  "))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("nickname");

		user.withdraw(Instant.now());
		userRepository.save(user);

		assertThatThrownBy(() -> getMyProfileUseCase.execute(user.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("deleted");
		assertThatThrownBy(() -> updateMyNicknameUseCase.execute(user.getId(), "x"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("deleted");
	}
}
