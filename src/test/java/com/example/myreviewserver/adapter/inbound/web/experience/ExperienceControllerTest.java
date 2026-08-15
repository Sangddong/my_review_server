package com.example.myreviewserver.adapter.inbound.web.experience;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class ExperienceControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void listsUpcomingAndCompletedForAuthenticatedUser() throws Exception {
		User user = userRepository.save(User.create("exp-api@test.com", "expUser"));
		String jwt = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

		Experience upcoming = experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(ExperiencePlatform.of(10L, true))
		));
		Experience completed = experienceRepository.save(Experience.create(
			user.getId(),
			"끝난 체험",
			ExperienceType.DELIVERY,
			LocalDate.of(2026, 8, 1),
			null,
			LocalDate.of(2026, 8, 5),
			null,
			List.of(ExperiencePlatform.of(20L, false))
		));
		completed.submitReview();
		experienceRepository.save(completed);

		mockMvc.perform(get("/api/experiences/upcoming"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/experiences/upcoming")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].id").value(upcoming.getId()))
			.andExpect(jsonPath("$.data[0].name").value("성수 카페"))
			.andExpect(jsonPath("$.data[0].experienceType").value("VISIT"))
			.andExpect(jsonPath("$.data[0].reviewSubmitted").value(false))
			.andExpect(jsonPath("$.data[0].requiredItemsComplete").value(false))
			.andExpect(jsonPath("$.data[0].platforms[0].platformId").value(10));

		mockMvc.perform(get("/api/experiences/completed")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].id").value(completed.getId()))
			.andExpect(jsonPath("$.data[0].reviewSubmitted").value(true));
	}

	@Test
	void getsExperienceDetailForAuthenticatedOwner() throws Exception {
		User owner = userRepository.save(User.create("exp-detail@test.com", "owner"));
		User other = userRepository.save(User.create("exp-detail-other@test.com", "other"));
		String ownerJwt = jwtTokenProvider.createAccessToken(owner.getId(), owner.getNickname());
		String otherJwt = jwtTokenProvider.createAccessToken(other.getId(), other.getNickname());

		Experience experience = experienceRepository.save(Experience.create(
			owner.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			LocalDate.of(2026, 8, 20),
			LocalTime.of(14, 0),
			LocalDate.of(2026, 8, 25),
			"https://example.com",
			List.of(ExperiencePlatform.of(10L, true), ExperiencePlatform.of(20L, false))
		));
		experience.setPlatformRegistered(10L, true);
		experience = experienceRepository.save(experience);

		mockMvc.perform(get("/api/experiences/" + experience.getId()))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/experiences/" + experience.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerJwt))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(experience.getId()))
			.andExpect(jsonPath("$.data.name").value("성수 카페"))
			.andExpect(jsonPath("$.data.experienceType").value("VISIT"))
			.andExpect(jsonPath("$.data.reviewSubmitted").value(false))
			.andExpect(jsonPath("$.data.requiredItemsComplete").value(true))
			.andExpect(jsonPath("$.data.platforms[0].platformId").value(10))
			.andExpect(jsonPath("$.data.platforms[0].required").value(true))
			.andExpect(jsonPath("$.data.platforms[0].registered").value(true))
			.andExpect(jsonPath("$.data.platforms[1].platformId").value(20))
			.andExpect(jsonPath("$.data.platforms[1].required").value(false))
			.andExpect(jsonPath("$.data.platforms[1].registered").value(false));

		mockMvc.perform(get("/api/experiences/" + experience.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherJwt))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("Experience not found"));

		mockMvc.perform(get("/api/experiences/999999")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerJwt))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Experience not found"));
	}
}
