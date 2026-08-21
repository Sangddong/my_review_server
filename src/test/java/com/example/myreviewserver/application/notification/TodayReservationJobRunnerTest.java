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
import java.time.LocalTime;
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
class TodayReservationJobRunnerTest {

	@Autowired
	TodayReservationJobRunner todayReservationJobRunner;

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
	void sendsTodayPushAndRecordsForExperienceReservedToday(CapturedOutput output) {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("today-runner@test.com", "todayrunner"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"넥쿨러",
			ExperienceType.VISIT,
			today,
			LocalTime.of(14, 0),
			today.plusDays(7),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "today-token-a", DevicePlatform.IOS));

		todayReservationJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(TodayReservationJobRunner.RULE_KEY)
		)).hasSize(1);
		assertThat(output.getOut()).contains("넥쿨러 오늘 체험 일정이 있어요");
		assertThat(output.getOut()).contains("오늘 체험할 일정을 확인해보세요");
	}

	@Test
	void doesNotSendWhenReservationDateIsNotToday() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("today-wrong@test.com", "todaywrong"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"내일 예약 체험",
			ExperienceType.VISIT,
			today.plusDays(1),
			LocalTime.of(11, 0),
			today.plusDays(8),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "today-wrong-token", DevicePlatform.ANDROID));

		todayReservationJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(TodayReservationJobRunner.RULE_KEY)
		)).isEmpty();
	}

	@Test
	void doesNotSendWhenReservationDateIsNull() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		Instant now = today.atStartOfDay(ZoneOffset.UTC).toInstant();

		User user = userRepository.save(User.create("today-null@test.com", "todaynull"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "유튜브", "#333333", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"예약일 없는 체험",
			ExperienceType.VISIT,
			null,
			null,
			today.plusDays(7),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "today-null-token", DevicePlatform.IOS));

		todayReservationJobRunner.run(now);

		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of(TodayReservationJobRunner.RULE_KEY)
		)).isEmpty();
	}
}
