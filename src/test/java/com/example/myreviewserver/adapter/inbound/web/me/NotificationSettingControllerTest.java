package com.example.myreviewserver.adapter.inbound.web.me;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class NotificationSettingControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Test
	void listAndUpdateRequireAuthAndReturnEveryRule() throws Exception {
		User user = userRepository.save(User.create("setting-api@test.com", "settingapi"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		mockMvc.perform(get("/api/me/notification-settings"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/me/notification-settings")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].ruleKey").value("D3"))
			.andExpect(jsonPath("$.data[0].enabled").value(true));

		mockMvc.perform(patch("/api/me/notification-settings")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"settingList":[{"ruleKey":"D3","enabled":false}]}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].ruleKey").value("D3"))
			.andExpect(jsonPath("$.data[0].enabled").value(false))
			.andExpect(jsonPath("$.data[1].enabled").value(true));

		mockMvc.perform(get("/api/me/notification-settings")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].enabled").value(false));
	}

	@Test
	void rejectsEmptySettingList() throws Exception {
		User user = userRepository.save(User.create("setting-api-bad@test.com", "settingapibad"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		mockMvc.perform(patch("/api/me/notification-settings")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"settingList":[]}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}
}
