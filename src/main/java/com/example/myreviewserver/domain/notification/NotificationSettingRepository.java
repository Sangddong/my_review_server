package com.example.myreviewserver.domain.notification;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for per-user notification rule settings.
 */
public interface NotificationSettingRepository {

	NotificationSetting save(NotificationSetting notificationSetting);

	Optional<NotificationSetting> findByUserIdAndRuleKey(Long userId, NotificationRuleKey ruleKey);

	List<NotificationSetting> findAllByUserId(Long userId);

	List<NotificationSetting> findDisabledByUserIdIn(List<Long> userIdList);
}
