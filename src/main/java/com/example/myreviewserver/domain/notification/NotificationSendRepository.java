package com.example.myreviewserver.domain.notification;

import java.util.List;

/**
 * Persistence port for notification send records.
 */
public interface NotificationSendRepository {

	List<NotificationSend> findByExperienceIdInAndRuleKeyIn(List<Long> experienceIdList, List<String> ruleKeyList);

	void saveAll(List<NotificationSend> notificationSendList);
}
