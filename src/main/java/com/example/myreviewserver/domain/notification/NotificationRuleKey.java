package com.example.myreviewserver.domain.notification;

import com.example.myreviewserver.domain.shared.DomainException;

/**
 * Push notification rules a user can turn on or off.
 * Values match the ruleKey strings used by the dispatch job runners.
 */
public enum NotificationRuleKey {

	D3,
	TODAY,
	OVERDUE;

	public static NotificationRuleKey from(String value) {
		if (value == null || value.isBlank()) {
			throw new DomainException("ruleKey is required");
		}
		try {
			return valueOf(value.trim().toUpperCase());
		}
		catch (IllegalArgumentException e) {
			throw new DomainException("Unknown ruleKey: " + value);
		}
	}
}
