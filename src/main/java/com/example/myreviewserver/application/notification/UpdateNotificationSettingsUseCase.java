package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import com.example.myreviewserver.domain.notification.NotificationSetting;
import com.example.myreviewserver.domain.notification.NotificationSettingRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upserts on/off state for the given notification rules and returns the full setting list.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdateNotificationSettingsUseCase {

	private final NotificationSettingRepository notificationSettingRepository;
	private final GetNotificationSettingsUseCase getNotificationSettingsUseCase;

	public UpdateNotificationSettingsUseCase(
		NotificationSettingRepository notificationSettingRepository,
		GetNotificationSettingsUseCase getNotificationSettingsUseCase
	) {
		this.notificationSettingRepository = notificationSettingRepository;
		this.getNotificationSettingsUseCase = getNotificationSettingsUseCase;
	}

	public List<NotificationRuleSetting> execute(Long userId, List<NotificationRuleSetting> settingList) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (settingList == null || settingList.isEmpty()) {
			throw new DomainException("settingList is required");
		}

		Set<NotificationRuleKey> seen = EnumSet.noneOf(NotificationRuleKey.class);
		for (NotificationRuleSetting requested : settingList) {
			if (requested == null || requested.ruleKey() == null) {
				throw new DomainException("ruleKey is required");
			}
			if (!seen.add(requested.ruleKey())) {
				throw new DomainException("Duplicate ruleKey: " + requested.ruleKey());
			}
		}

		for (NotificationRuleSetting requested : settingList) {
			NotificationSetting setting = notificationSettingRepository
				.findByUserIdAndRuleKey(userId, requested.ruleKey())
				.orElseGet(() -> NotificationSetting.create(userId, requested.ruleKey(), requested.enabled()));
			setting.changeEnabled(requested.enabled());
			notificationSettingRepository.save(setting);
		}

		return getNotificationSettingsUseCase.execute(userId);
	}
}
