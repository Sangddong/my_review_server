package com.example.myreviewserver.application.devicetoken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RegisterDeviceTokenUseCaseTest {

	@Autowired
	RegisterDeviceTokenUseCase registerDeviceTokenUseCase;

	@Autowired
	DeleteDeviceTokenUseCase deleteDeviceTokenUseCase;

	@Autowired
	DeviceTokenRepository deviceTokenRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	void registersUpdatesAndDeletesToken() {
		User user = userRepository.save(User.create("u@test.com", "user1"));
		User other = userRepository.save(User.create("o@test.com", "other"));

		DeviceToken created = registerDeviceTokenUseCase.execute(
			user.getId(),
			"token-abc",
			DevicePlatform.ANDROID
		);
		assertThat(created.getId()).isNotNull();
		assertThat(created.getUserId()).isEqualTo(user.getId());
		assertThat(deviceTokenRepository.findAllByUserId(user.getId())).hasSize(1);

		DeviceToken reassigned = registerDeviceTokenUseCase.execute(
			other.getId(),
			"token-abc",
			DevicePlatform.IOS
		);
		assertThat(reassigned.getId()).isEqualTo(created.getId());
		assertThat(reassigned.getUserId()).isEqualTo(other.getId());
		assertThat(reassigned.getPlatform()).isEqualTo(DevicePlatform.IOS);
		assertThat(deviceTokenRepository.findAllByUserId(user.getId())).isEmpty();
		assertThat(deviceTokenRepository.findAllByUserId(other.getId())).hasSize(1);

		deleteDeviceTokenUseCase.execute(other.getId(), "token-abc");
		assertThat(deviceTokenRepository.findByToken("token-abc")).isEmpty();
	}

	@Test
	void rejectsBlankToken() {
		assertThatThrownBy(() -> registerDeviceTokenUseCase.execute(1L, " ", DevicePlatform.WEB))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("token");
	}
}
