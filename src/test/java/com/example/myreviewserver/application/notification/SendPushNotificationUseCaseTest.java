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
import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import com.example.myreviewserver.domain.notification.NotificationSend;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.LocalDate;
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

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;

	@Test
	void recordsSendAndInboxAndSkipsDuplicateRuleForSameExperience(CapturedOutput output) {
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
			"성수 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		);

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isEqualTo(1);
		List<NotificationSend> first = notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("D3")
		);
		assertThat(first).hasSize(1);
		assertThat(first.get(0).getUserId()).isEqualTo(user.getId());
		assertThat(first.get(0).getRuleKey()).isEqualTo("D3");

		List<Notification> inbox = notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId());
		assertThat(inbox).hasSize(1);
		assertThat(inbox.get(0).getExperienceId()).isEqualTo(experience.getId());
		assertThat(inbox.get(0).getRuleKey()).isEqualTo("D3");
		assertThat(inbox.get(0).getTitle()).isEqualTo("성수 카페 리뷰 마감 3일 전입니다");
		assertThat(inbox.get(0).getBody()).isEqualTo("마감일 전에 리뷰를 작성하여 제출해주세요");
		assertThat(inbox.get(0).isRead()).isFalse();
		assertThat(output.getOut()).contains("ruleKey=D3");
		assertThat(output.getOut()).contains("experienceId=" + experience.getId());
		assertThat(output.getOut()).contains("screen=experience_detail");

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isEqualTo(0);
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("D3")
		)).hasSize(1);
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).hasSize(1);
	}

	@Test
	void skipsRuleTurnedOffAndSendsAgainWhenTurnedBackOn() {
		User user = userRepository.save(User.create("notify-off@test.com", "notify-off"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "블로그", "#333333", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"연남 카페",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 27),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "notify-off-token", DevicePlatform.IOS));

		updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(new NotificationRuleSetting(NotificationRuleKey.D3, false))
		);

		NotificationDispatchCommand command = new NotificationDispatchCommand(
			user.getId(),
			experience.getId(),
			"D3",
			"연남 카페 리뷰 마감 3일 전입니다",
			"마감일 전에 리뷰를 작성하여 제출해주세요"
		);

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isZero();
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("D3")
		)).isEmpty();
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).isEmpty();

		updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(new NotificationRuleSetting(NotificationRuleKey.D3, true))
		);

		assertThat(sendPushNotificationUseCase.execute(List.of(command))).isEqualTo(1);
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).hasSize(1);
	}

	@Test
	void keepsSendingRulesThatAreStillOn() {
		User user = userRepository.save(User.create("notify-mixed@test.com", "notify-mixed"));
		Platform platform = platformRepository.save(Platform.create(user.getId(), "인스타", "#444444", 0));
		Experience experience = experienceRepository.save(Experience.create(
			user.getId(),
			"압구정 식당",
			ExperienceType.VISIT,
			null,
			null,
			LocalDate.of(2026, 8, 29),
			null,
			List.of(ExperiencePlatform.of(platform.getId(), true))
		));
		deviceTokenRepository.save(DeviceToken.create(user.getId(), "notify-mixed-token", DevicePlatform.ANDROID));

		updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(new NotificationRuleSetting(NotificationRuleKey.D3, false))
		);

		int sent = sendPushNotificationUseCase.execute(List.of(
			new NotificationDispatchCommand(
				user.getId(),
				experience.getId(),
				"D3",
				"압구정 식당 리뷰 마감 3일 전입니다",
				"마감일 전에 리뷰를 작성하여 제출해주세요"
			),
			new NotificationDispatchCommand(
				user.getId(),
				experience.getId(),
				"TODAY",
				"압구정 식당 오늘 체험 일정이 있어요",
				"오늘 체험할 일정을 확인해보세요"
			)
		));

		assertThat(sent).isEqualTo(1);
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId()))
			.extracting(Notification::getRuleKey)
			.containsExactly("TODAY");
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
				"한남 식당 오늘 체험 일정이 있어요",
				"오늘 체험할 일정을 확인해보세요"
			)
		));

		assertThat(sent).isEqualTo(0);
		assertThat(notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			List.of(experience.getId()),
			List.of("TODAY")
		)).isEmpty();
		assertThat(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId())).isEmpty();
	}
}
