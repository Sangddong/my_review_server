package com.example.myreviewserver.adapter.outbound.persistence.notification;

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
class NotificationRepositoryAdapterTest {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void savesFindsMarksReadCountsAndDeletes() {
		User user = userRepository.save(User.create("notify-inbox@test.com", "notify-inbox"));
		User other = userRepository.save(User.create("notify-other@test.com", "notify-other"));
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

		Notification first = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		Notification second = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"TODAY",
			"성수 카페 오늘 체험 일정이 있어요",
			"오늘 예약된 체험을 확인해보세요"
		));

		assertThat(first.getId()).isNotNull();
		assertThat(first.getCreatedAt()).isNotNull();
		assertThat(first.isRead()).isFalse();
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isEqualTo(2);
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId()))
			.extracting(Notification::getId)
			.containsExactly(second.getId(), first.getId());
		assertThat(notificationRepository.findByIdAndUserId(first.getId(), other.getId())).isEmpty();

		first.markRead();
		Notification read = notificationRepository.save(first);
		assertThat(read.isRead()).isTrue();
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isEqualTo(1);

		notificationRepository.softDeleteByUserIdAndIdIn(user.getId(), List.of(second.getId()));
		assertThat(notificationRepository.findByIdAndUserId(second.getId(), user.getId())).isEmpty();
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).hasSize(1);
		assertThat(notificationRepository.countUnreadByUserId(user.getId())).isZero();
	}
}
