package com.example.myreviewserver.adapter.inbound.web.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DeviceTokenRepository deviceTokenRepository;

	@Test
	void withdrawRequiresAuthAndSoftDeletesOwnAccount() throws Exception {
		User user = userRepository.save(User.create("me-withdraw@test.com", "meWithdraw"));
		userRepository.saveOauthAccount(user.getId(), AuthProvider.GOOGLE, "google-me-withdraw");
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "me-withdraw-token", DevicePlatform.WEB));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		mockMvc.perform(delete("/api/me"))
			.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isNoContent());

		User withdrawn = userRepository.findById(user.getId()).orElseThrow();
		assertThat(withdrawn.getIsDeleted()).isEqualTo(1);
		assertThat(withdrawn.getDeletedAt()).isNotNull();
		assertThat(deviceTokenRepository.findAllByUserId(user.getId())).isEmpty();
		assertThat(userRepository.findByProvider(AuthProvider.GOOGLE, "google-me-withdraw")).isEmpty();

		// Soft-deleted user JWT must no longer authenticate protected APIs
		mockMvc.perform(get("/api/me/notifications")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isForbidden());
	}
}
