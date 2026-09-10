package com.example.myreviewserver.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WithdrawUserUseCaseTest {

	@Autowired
	WithdrawUserUseCase withdrawUserUseCase;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DeviceTokenRepository deviceTokenRepository;

	@Test
	void softDeletesUserRemovesTokensAndUnlinksOauth() {
		User user = userRepository.save(User.create("withdraw@test.com", "withdrawer"));
		userRepository.saveOauthAccount(user.getId(), AuthProvider.KAKAO, "kakao-withdraw-1");
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "withdraw-token-a", DevicePlatform.IOS));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "withdraw-token-b", DevicePlatform.ANDROID));

		withdrawUserUseCase.execute(user.getId());

		User withdrawn = userRepository.findById(user.getId()).orElseThrow();
		assertThat(withdrawn.getIsDeleted()).isEqualTo(1);
		assertThat(withdrawn.getDeletedAt()).isNotNull();
		assertThat(deviceTokenRepository.findAllByUserId(user.getId())).isEmpty();
		assertThat(userRepository.findByProvider(AuthProvider.KAKAO, "kakao-withdraw-1")).isEmpty();

		assertThatThrownBy(() -> withdrawUserUseCase.execute(user.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("deleted");
	}

	@Test
	void rejectsMissingUser() {
		assertThatThrownBy(() -> withdrawUserUseCase.execute(9_999_999L))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not found");
	}
}
