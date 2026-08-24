package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
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
class MarkNotificationReadUseCaseTest {

	@Autowired
	MarkNotificationReadUseCase markNotificationReadUseCase;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void marksOwnNotificationReadAndIsIdempotent() {
		User user = userRepository.save(User.create("mark-read@test.com", "markread"));
		User other = userRepository.save(User.create("mark-read-other@test.com", "markreadother"));
		Experience experience = saveExperience(user, "블로그", "#111111");

		Notification notification = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isEqualTo(1);

		Notification read = markNotificationReadUseCase.execute(user.getId(), notification.getId());
		assertThat(read.isRead()).isTrue();
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isZero();

		Notification again = markNotificationReadUseCase.execute(user.getId(), notification.getId());
		assertThat(again.isRead()).isTrue();
		assertThat(again.getIsRead()).isEqualTo(1);

		assertThatThrownBy(() -> markNotificationReadUseCase.execute(other.getId(), notification.getId()))
			.isInstanceOf(DomainException.class)
			.hasMessage("Notification not found");
		assertThatThrownBy(() -> markNotificationReadUseCase.execute(user.getId(), 9_999_999L))
			.isInstanceOf(DomainException.class)
			.hasMessage("Notification not found");
	}

	private Experience saveExperience(User user, String platformName, String color) {
		Platform platform = platformRepository.save(Platform.create(user.getId(), platformName, color, 0));
		return experienceRepository.save(Experience.create(
			user.getId(),
			"성수 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 25),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
	}
}
