package com.example.myreviewserver.adapter.inbound.web.platform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
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
class PlatformControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Test
	void listsActivePlatformsForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("plat@test.com", "platUser"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		platformRepository.save(Platform.create(user.getId(), "블로그", "#c6f8c8", 0));
		Platform hidden = platformRepository.save(Platform.create(user.getId(), "숨김", "#dddddd", 1));
		hidden.softDelete();
		platformRepository.save(hidden);

		mockMvc.perform(get("/api/platforms"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/platforms")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].name").value("블로그"))
			.andExpect(jsonPath("$.data[0].color").value("#c6f8c8"))
			.andExpect(jsonPath("$.data[0].sortOrder").value(0))
			.andExpect(jsonPath("$.data[0].id").isNumber());
	}

	@Test
	void createsPlatformForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("create-api@test.com", "creator"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		mockMvc.perform(post("/api/platforms")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"블로그","color":"#c6f8c8"}
					"""))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/platforms")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"블로그","color":"#c6f8c8"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.name").value("블로그"))
			.andExpect(jsonPath("$.data.color").value("#c6f8c8"))
			.andExpect(jsonPath("$.data.sortOrder").value(0))
			.andExpect(jsonPath("$.data.id").isNumber());
	}
}
