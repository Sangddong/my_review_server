package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.notification.NotificationRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SendPushNotificationUseCasePushFailureTest {

	@MockitoBean
	PushSender pushSender;

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

	@Autowired
	NotificationRepository notificationRepository;

	@Test
	void keepsInboxWhenPushDeliveryFails() {
		willThrow(new IllegalStateException("FCM unavailable"))
			.given(pushSender).send(anyString(), any());

		User user = userRepository.save(User.create("push-fail@test.com", "pushfail"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#111111", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"연남 디저트",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 26),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "push-fail-token", DevicePlatform.IOS));

		int sent = sendPushNotificationUseCase.execute(List.of(new NotificationDispatchCommand(
			user.getId(),
			experience.getId(),
			"OVERDUE",
			"연남 디저트 리뷰 제출 기한이 초과되었습니다",
			"리뷰 제출을 서둘러주세요"
		)));

		assertThat(sent).isEqualTo(1);
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).hasSize(1);
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("OVERDUE")
		)).hasSize(1);
	}
}
