package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.NotificationSend;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
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
class SendPushNotificationUseCaseTest {

	@Autowired
	SendPushNotificationUseCase sendPushNotificationUseCase;

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
	void recordsSendAndSkipsDuplicateRuleForSameExperience() {
		User user = userRepository.save(User.create("notify-send@test.com", "notify-send"));
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
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "notify-token-a", DevicePlatform.IOS));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "notify-token-b", DevicePlatform.ANDROID));

		NotificationDispatchCommand command = new NotificationDispatchCommand(
			user.getId(),
			experience.getId(),
			"D3",
			"리뷰 제출일이 임박한 체험이 있어요",
			"마감 3일 전입니다"
		);

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isEqualTo(1);
		List<NotificationSend> first = notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("D3")
		);
		assertThat(first).hasSize(1);
		assertThat(first.get(0).getUserId()).isEqualTo(user.getId());
		assertThat(first.get(0).getRuleKey()).isEqualTo("D3");

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isEqualTo(0);
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("D3")
		)).hasSize(1);
	}

	@Test
	void skipsWhenUserHasNoDeviceTokens() {
		User user = userRepository.save(User.create("notify-notoken@test.com", "notify-notoken"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "인스타", "#222222", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"한남 식당",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 26),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));

		int sent = sendPushNotificationUseCase.execute(List.of(
			new NotificationDispatchCommand(
				user.getId(),
				experience.getId(),
				"TODAY",
				"오늘 체험할 일정을 확인해보세요",
				"오늘 예약이 있습니다"
			)
		));

		assertThat(sent).isEqualTo(0);
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("TODAY")
		)).isEmpty();
	}
}
