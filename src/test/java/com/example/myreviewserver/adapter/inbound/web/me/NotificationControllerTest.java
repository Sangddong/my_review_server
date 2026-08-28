package com.example.myreviewserver.adapter.inbound.web.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

	@Test
	void markReadRequiresAuthAndUpdatesOwnNotification() throws Exception {
		User user = userRepository.save(User.create("notify-read-api@test.com", "notifyreadapi"));
		User other = userRepository.save(User.create("notify-read-other@test.com", "notifyreadother"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		String otherToken = jwtTokenProvider.createAccessToken(other.getId(), other.getNickname());
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#222222", 0));
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
		String path = "/api/me/notifications/" + notification.getId() + "/read";

		mockMvc.perform(patch(path))
			.andExpect(status().isForbidden());

		mockMvc.perform(patch(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));

		mockMvc.perform(patch(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(notification.getId().intValue()))
			.andExpect(jsonPath("$.data.isRead").value(true));

		mockMvc.perform(patch(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.isRead").value(true));
	}

	@Test
	void markAllReadRequiresAuthAndMarksOnlyOwnUnread() throws Exception {
		User user = userRepository.save(User.create("notify-read-all-api@test.com", "notifyreadallapi"));
		User other = userRepository.save(User.create("notify-read-all-other@test.com", "notifyreadallother"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		Platform userPlatform = platformRepository.save(Platform.create(user.getId(), "블로그", "#333333", 0));
		Experience userExperience = experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(userPlatform.getId(), true))
		));
		Platform otherPlatform = platformRepository.save(Platform.create(other.getId(), "인스타", "#444444", 0));
		Experience otherExperience = experienceRepository.save(Experience.create(
			other.getId(),
			"한남 식당",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 26),
			null,
			List.of(ExperiencePlatform.of(otherPlatform.getId(), true))
		));
		notificationRepository.save(Notification.create(
			user.getId(),
			userExperience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		Notification otherUnread = notificationRepository.save(Notification.create(
			other.getId(),
			otherExperience.getId(),
			"TODAY",
			"한남 식당 오늘 체험 일정이 있어요",
			"오늘 체험할 일정을 확인해보세요"
		));

		mockMvc.perform(patch("/api/me/notifications/read-all"))
			.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/me/notifications/read-all")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		mockMvc.perform(get("/api/me/notifications")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].isRead").value(true));

		assertThat(notificationRepository.findByIdAndUserId(otherUnread.getId(), other.getId()))
			.get()
			.extracting(Notification::isRead)
			.isEqualTo(false);

		mockMvc.perform(patch("/api/me/notifications/read-all")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk());
	}

	@Test
	void deleteRequiresAuthAndRemovesOwnNotification() throws Exception {
		User user = userRepository.save(User.create("notify-delete-api@test.com", "notifydeleteapi"));
		User other = userRepository.save(User.create("notify-delete-other@test.com", "notifydeleteother"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		String otherToken = jwtTokenProvider.createAccessToken(other.getId(), other.getNickname());
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#555555", 0));
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
		String path = "/api/me/notifications/" + notification.getId();

		mockMvc.perform(delete(path))
			.andExpect(status().isForbidden());

		mockMvc.perform(delete(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));

		mockMvc.perform(delete(path)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isNoContent());

		assertThat(notificationRepository.findByIdAndUserId(notification.getId(), user.getId())).isEmpty();
	}
}
