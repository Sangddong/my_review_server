package com.example.myreviewserver.adapter.inbound.web.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

	@Test
	void updatesPlatformForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("update-api@test.com", "updater"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#c6f8c8", 0));

		mockMvc.perform(patch("/api/platforms/" + platform.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"브런치"}
					"""))
			.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/platforms/" + platform.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"브런치","color":"#112233"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.name").value("브런치"))
			.andExpect(jsonPath("$.data.color").value("#112233"))
			.andExpect(jsonPath("$.data.id").value(platform.getId()));

		mockMvc.perform(patch("/api/platforms/" + platform.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());

		platform.softDelete();
		platformRepository.save(platform);

		mockMvc.perform(patch("/api/platforms/" + platform.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"삭제됨"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Platform not found"));
	}

	@Test
	void softDeletesPlatformForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("delete-api@test.com", "deleter"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#c6f8c8", 0));
		platformRepository.save(Platform.create(user.getId(), "유튜브", "#f8dac6", 1));

		mockMvc.perform(delete("/api/platforms/" + platform.getId()))
			.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/platforms/" + platform.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/platforms")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].name").value("유튜브"));

		assertThat(platformRepository.findById(platform.getId()).orElseThrow().isActive()).isFalse();

		mockMvc.perform(delete("/api/platforms/" + platform.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Platform not found"));
	}

	@Test
	void reordersPlatformsForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("reorder-api@test.com", "reord"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		Platform first = platformRepository.save(Platform.create(user.getId(), "블로그", "#c6f8c8", 0));
		Platform second = platformRepository.save(Platform.create(user.getId(), "유튜브", "#f8dac6", 1));

		mockMvc.perform(put("/api/platforms/reorder")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderedIds":[%d,%d]}
					""".formatted(second.getId(), first.getId())))
			.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/platforms/reorder")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderedIds":[%d,%d]}
					""".formatted(second.getId(), first.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].id").value(second.getId()))
			.andExpect(jsonPath("$.data[0].sortOrder").value(0))
			.andExpect(jsonPath("$.data[1].id").value(first.getId()))
			.andExpect(jsonPath("$.data[1].sortOrder").value(1));

		mockMvc.perform(put("/api/platforms/reorder")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderedIds":[%d]}
					""".formatted(first.getId())))
			.andExpect(status().isBadRequest());
	}
}
