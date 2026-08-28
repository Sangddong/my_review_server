package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import com.example.myreviewserver.domain.notification.NotificationSetting;
import com.example.myreviewserver.domain.notification.NotificationSettingRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns every notification rule with its on/off state for the authenticated user.
 * Rules without a saved row default to enabled.
 *
 * @Service: 서비스 빈.
 * @Transactional(readOnly = true): 읽기 전용 트랜잭션.
 */
@Service
@Transactional(readOnly = true)
public class GetNotificationSettingsUseCase {

	private final NotificationSettingRepository notificationSettingRepository;

	public GetNotificationSettingsUseCase(NotificationSettingRepository notificationSettingRepository) {
		this.notificationSettingRepository = notificationSettingRepository;
	}

	public List<NotificationRuleSetting> execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}

		Map<NotificationRuleKey, Boolean> savedByRuleKey = new EnumMap<>(NotificationRuleKey.class);
		for (NotificationSetting setting : notificationSettingRepository.findAllByUserId(userId)) {
			savedByRuleKey.put(setting.getRuleKey(), setting.isEnabled());
		}

		List<NotificationRuleSetting> settingList = new ArrayList<>();
		for (NotificationRuleKey ruleKey : NotificationRuleKey.values()) {
			settingList.add(new NotificationRuleSetting(ruleKey, savedByRuleKey.getOrDefault(ruleKey, true)));
		}
		return settingList;
	}
}
