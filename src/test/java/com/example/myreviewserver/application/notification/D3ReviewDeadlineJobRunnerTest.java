package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
class D3ReviewDeadlineJobRunnerTest {

	@Autowired
	D3ReviewDeadlineJobRunner d3ReviewDeadlineJobRunner;

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

	@Test
	void sendsD3PushAndRecordsForUnsubmittedExperienceDue3DaysLater(CapturedOutput output) {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		LocalDate deadline = today.plusDays(3);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("d3-runner@test.com", "d3runner"));
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
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "d3-token-a", DevicePlatform.IOS));

		d3ReviewDeadlineJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(D3ReviewDeadlineJobRunner.RULE_KEY)
		)).hasSize(1);
		assertThat(output.getOut()).contains("넥쿨러 리뷰 마감 3일 전입니다");
		assertThat(output.getOut()).contains("마감일 전에 리뷰를 작성하여 제출해주세요");
	}

	@Test
	void doesNotSendWhenDeadlineIsNot3DaysAway() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("d3-wrong@test.com", "d3wrong"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 0));
		experienceRepository.save(Experience.create(
			user.getId(),
			"D5 체험 (해당 없음)",
			ExperienceType.VISIT,
			null,
			null,
			today.plusDays(5),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "d3-wrong-token", DevicePlatform.ANDROID));

		d3ReviewDeadlineJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experienceRepository
				.findUpcomingByUserIdOrderByReservationAscIdAsc(user.getId())
				.get(0).getId()),
			List.of(D3ReviewDeadlineJobRunner.RULE_KEY)
		)).isEmpty();
	}

	@Test
	void doesNotSendWhenExperienceAlreadySubmitted() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		LocalDate deadline = today.plusDays(3);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("d3-submitted@test.com", "d3submitted"));
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
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "d3-submitted-token", DevicePlatform.IOS));

		d3ReviewDeadlineJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(D3ReviewDeadlineJobRunner.RULE_KEY)
		)).isEmpty();
	}
}
