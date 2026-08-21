package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ListNotificationsUseCaseTest {

	@Autowired
	ListNotificationsUseCase listNotificationsUseCase;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void returnsOwnNotificationsNewestFirstAndExcludesOthers() {
		User user = userRepository.save(User.create("list-notify@test.com", "listnotify"));
		User other = userRepository.save(User.create("list-other@test.com", "listother"));
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
		Platform otherPlatform = platformRepository.save(Platform.create(other.getId(), "인스타", "#222222", 0));
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

		Notification older = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		Notification newer = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"TODAY",
			"성수 카페 오늘 체험 일정이 있어요",
			"오늘 예약된 체험을 확인해보세요"
		));
		notificationRepository.save(Notification.create(
			other.getId(),
			otherExperience.getId(),
			"D3",
			"한남 식당 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));

		List<Notification> result = listNotificationsUseCase.execute(user.getId());
		assertThat(result).extracting(Notification::getId).containsExactly(newer.getId(), older.getId());
	}
}
