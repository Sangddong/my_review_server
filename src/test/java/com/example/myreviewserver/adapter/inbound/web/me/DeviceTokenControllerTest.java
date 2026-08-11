package com.example.myreviewserver.adapter.inbound.web.me;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeviceTokenControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Test
	void upsertAndDeleteRequireAuthAndSucceedWithJwt() throws Exception {
		User user = userRepository.save(User.create("device@test.com", "deviceUser"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		mockMvc.perform(put("/api/me/device-tokens")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"token":"fcm-1","platform":"ANDROID"}
					"""))
			.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/me/device-tokens")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"token":"fcm-1","platform":"ANDROID"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.token").value("fcm-1"))
			.andExpect(jsonPath("$.data.platform").value("ANDROID"))
			.andExpect(jsonPath("$.data.id").isNumber());

		mockMvc.perform(delete("/api/me/device-tokens")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"token":"fcm-1"}
					"""))
			.andExpect(status().isNoContent());
	}
}
