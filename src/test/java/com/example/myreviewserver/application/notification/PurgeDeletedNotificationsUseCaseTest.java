package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.adapter.outbound.persistence.notification.NotificationJpaEntity;
import com.example.myreviewserver.adapter.outbound.persistence.notification.SpringDataNotificationRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PurgeDeletedNotificationsUseCaseTest {

	@Autowired
	PurgeDeletedNotificationsUseCase purgeDeletedNotificationsUseCase;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	SpringDataNotificationRepository springDataNotificationRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Test
	void hardDeletesExpiredSoftDeletedNotificationsAndKeepsRecent() {
		User user = userRepository.save(User.create("notify-purge@test.com", "notifypurge"));
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
		Notification expired = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"D3",
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		));
		Notification recent = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"TODAY",
			"성수 카페 오늘 체험 일정이 있어요",
			"오늘 예약된 체험을 확인해보세요"
		));
		Notification active = notificationRepository.save(Notification.create(
			user.getId(),
			experience.getId(),
			"OVERDUE",
			"성수 카페 리뷰 마감일이 지났어요",
			"리뷰를 작성하여 제출해주세요"
		));

		notificationRepository.softDeleteByUserIdAndIdIn(user.getId(), List.of(expired.getId(), recent.getId()));

		Instant expiredDeletedAt = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(2).toInstant();
		NotificationJpaEntity expiredEntity = springDataNotificationRepository.findById(expired.getId()).orElseThrow();
		expiredEntity.setDeletedAt(expiredDeletedAt);
		springDataNotificationRepository.save(expiredEntity);

		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant();
		int deleted = purgeDeletedNotificationsUseCase.execute(cutoff);

		assertThat(deleted).isEqualTo(1);
		assertThat(springDataNotificationRepository.findById(expired.getId())).isEmpty();
		assertThat(springDataNotificationRepository.findById(recent.getId())).isPresent();
		assertThat(notificationRepository.findByIdAndUserId(active.getId(), user.getId())).isPresent();
	}
}
