package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class OverdueReviewDeadlineJobRunnerTest {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	@Autowired
	OverdueReviewDeadlineJobRunner overdueReviewDeadlineJobRunner;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PlatformRepository platformRepository;

	@Autowired
	ExperienceRepository experienceRepository;

	@Autowired
	DeviceTokenRepository deviceTokenRepository;

	@Autowired
	NotificationSendRepository notificationSendRepository;

	@Autowired
	NotificationRepository notificationRepository;

	@Test
	void sendsOverduePushAndRecordsForUnsubmittedExperiencePastDeadline(CapturedOutput output) {
		LocalDate today = LocalDate.now(SEOUL);
		LocalDate deadline = today.minusDays(1);

		User user = userRepository.save(User.create("overdue-runner@test.com", "overduerunner"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"넥쿨러",
			ExperienceType.VISIT,
			null,
			null,
			deadline,
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "overdue-token-a", DevicePlatform.IOS));

		overdueReviewDeadlineJobRunner.run(today);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(OverdueReviewDeadlineJobRunner.RULE_KEY)
		)).hasSize(1);
		List<Notification> inbox = notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId());
		assertThat(inbox).hasSize(1);
		assertThat(inbox.get(0).getExperienceId()).isEqualTo(experience.getId());
		assertThat(inbox.get(0).getRuleKey()).isEqualTo(OverdueReviewDeadlineJobRunner.RULE_KEY);
		assertThat(inbox.get(0).getTitle()).isEqualTo("넥쿨러 리뷰 제출 기한이 초과되었습니다");
		assertThat(inbox.get(0).getBody()).isEqualTo("리뷰 제출을 서둘러주세요");
		assertThat(inbox.get(0).getIsRead()).isNull();
		assertThat(output.getOut()).contains("넥쿨러 리뷰 제출 기한이 초과되었습니다");
		assertThat(output.getOut()).contains("리뷰 제출을 서둘러주세요");
	}

	@Test
	void doesNotSendWhenDeadlineIsTodayOrLater() {
		LocalDate today = LocalDate.now(SEOUL);

		User user = userRepository.save(User.create("overdue-future@test.com", "overduefuture"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 0));
		Experience todayDeadline = experienceRepository.save(Experience.create(
			user.getId(),
			"오늘 마감 체험",
			ExperienceType.VISIT,
			null,
			null,
			today,
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		Experience futureDeadline = experienceRepository.save(Experience.create(
			user.getId(),
			"내일 마감 체험",
			ExperienceType.VISIT,
			null,
			null,
			today.plusDays(1),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "overdue-future-token", DevicePlatform.ANDROID));

		overdueReviewDeadlineJobRunner.run(today);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(todayDeadline.getId(), futureDeadline.getId()),
			List.of(OverdueReviewDeadlineJobRunner.RULE_KEY)
		)).isEmpty();
	}

	@Test
	void doesNotSendWhenExperienceAlreadySubmitted() {
		LocalDate today = LocalDate.now(SEOUL);
		LocalDate deadline = today.minusDays(2);

		User user = userRepository.save(User.create("overdue-submitted@test.com", "overduesubmitted"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "유튜브", "#333333", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"이미 제출된 체험",
			ExperienceType.VISIT,
			null,
			null,
			deadline,
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		experience.submitReview();
		experienceRepository.save(experience);
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "overdue-submitted-token", DevicePlatform.IOS));

		overdueReviewDeadlineJobRunner.run(today);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(OverdueReviewDeadlineJobRunner.RULE_KEY)
		)).isEmpty();
	}
}
