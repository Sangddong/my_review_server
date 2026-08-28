package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;

/**
 * One rule and whether the user receives it. Unsaved rules resolve to enabled.
 */
public record NotificationRuleSetting(
	NotificationRuleKey ruleKey,
	boolean enabled
) {
}
