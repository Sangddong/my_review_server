package com.example.myreviewserver.adapter.inbound.web.me;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.LocalDate;
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
class NotificationControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	NotificationRepository notificationRepository;

	@Test
	void listRequiresAuthAndReturnsOwnNotifications() throws Exception {
		User user = userRepository.save(User.create("notify-api@test.com", "notifyapi"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		Notification notification = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));

		mockMvc.perform(get("/api/me/notifications"))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/me/notifications")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].id").value(notification.getId().intValue()))
			.andExpect(jsonPath("$.data[0].experienceId").value(experience.getId().intValue()))
			.andExpect(jsonPath("$.data[0].ruleKey").value("D3"))
			.andExpect(jsonPath("$.data[0].title").value("성수 카페 리뷰 마감 3일 전입니다"))
			.andExpect(jsonPath("$.data[0].body").value("마감일 전에 리뷰를 작성하여 제출해주세요"))
			.andExpect(jsonPath("$.data[0].isRead").value(false))
			.andExpect(jsonPath("$.data[0].createdAt").isNotEmpty());
	}
}
