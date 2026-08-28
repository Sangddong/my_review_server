package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationSettingsUseCaseTest {

	@Autowired
	GetNotificationSettingsUseCase getNotificationSettingsUseCase;

	@Autowired
	UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;

	@Autowired
	UserRepository userRepository;

	@Test
	void defaultsToEnabledForEveryRule() {
		User user = userRepository.save(User.create("setting-default@test.com", "settingdefault"));

		List<NotificationRuleSetting> settingList = getNotificationSettingsUseCase.execute(user.getId());

		assertThat(settingList)
			.extracting(NotificationRuleSetting::ruleKey)
			.containsExactly(NotificationRuleKey.D3, NotificationRuleKey.TODAY, NotificationRuleKey.OVERDUE);
		assertThat(settingList).allMatch(NotificationRuleSetting::enabled);
	}

	@Test
	void updatesOnlyRequestedRulesAndTogglesBackOn() {
		User user = userRepository.save(User.create("setting-update@test.com", "settingupdate"));

		List<NotificationRuleSetting> afterDisable = updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(new NotificationRuleSetting(NotificationRuleKey.D3, false))
		);

		assertThat(afterDisable)
			.filteredOn(setting -> setting.ruleKey() == NotificationRuleKey.D3)
			.singleElement()
			.extracting(NotificationRuleSetting::enabled)
			.isEqualTo(false);
		assertThat(afterDisable)
			.filteredOn(setting -> setting.ruleKey() != NotificationRuleKey.D3)
			.allMatch(NotificationRuleSetting::enabled);

		List<NotificationRuleSetting> afterEnable = updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(new NotificationRuleSetting(NotificationRuleKey.D3, true))
		);

		assertThat(afterEnable).allMatch(NotificationRuleSetting::enabled);
	}

	@Test
	void rejectsEmptyAndDuplicateRules() {
		User user = userRepository.save(User.create("setting-invalid@test.com", "settinginvalid"));

		assertThatThrownBy(() -> updateNotificationSettingsUseCase.execute(user.getId(), List.of()))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("settingList is required");

		assertThatThrownBy(() -> updateNotificationSettingsUseCase.execute(
			user.getId(),
			List.of(
				new NotificationRuleSetting(NotificationRuleKey.TODAY, false),
				new NotificationRuleSetting(NotificationRuleKey.TODAY, true)
			)
		))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("Duplicate ruleKey");
	}
}
