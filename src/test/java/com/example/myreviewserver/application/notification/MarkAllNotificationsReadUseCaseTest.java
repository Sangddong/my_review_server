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
class MarkAllNotificationsReadUseCaseTest {

	@Autowired
	MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void marksAllOwnUnreadAndLeavesOthersAndIsIdempotent() {
		User user = userRepository.save(User.create("mark-all-read@test.com", "markallread"));
		User other = userRepository.save(User.create("mark-all-other@test.com", "markallother"));
		Experience experience = saveExperience(user, "블로그", "#111111");
		Experience otherExperience = saveExperience(other, "인스타", "#222222");

		notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		Notification alreadyRead = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"TODAY",
			"성수 카페 오늘 체험 일정이 있어요",
			"오늘 체험할 일정을 확인해보세요"
		));
		alreadyRead.markRead();
		notificationRepository.save(alreadyRead);
		Notification otherUnread = notificationRepository.save(Notification.create(
			other.getId(),
			otherExperience.getId(),
			"D3",
			"한남 식당 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));

		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isEqualTo(1);
		assertThat(notificationRepository.countUnreadByUserId(other.getId())).isEqualTo(1);

		markAllNotificationsReadUseCase.execute(user.getId());
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isZero();
		assertThat(notificationRepository.findByIdAndUserId(otherUnread.getId(), other.getId()))
			.get()
			.extracting(Notification::isRead)
			.isEqualTo(false);

		markAllNotificationsReadUseCase.execute(user.getId());
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isZero();
	}

	private Experience saveExperience(User user, String platformName, String color) {
		Platform platform = platformRepository.save(Platform.create(user.getId(), platformName, color, 0));
		return experienceRepository.save(Experience.create(
			user.getId(),
			user.getNickname() + " 체험",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
	}
}
