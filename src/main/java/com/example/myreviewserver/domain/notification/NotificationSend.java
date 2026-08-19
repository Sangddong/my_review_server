package com.example.myreviewserver.domain.notification;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

/**
 * Record that a push was sent for one experience and notification rule.
 */
public class NotificationSend {

	private static final int RULE_KEY_MAX_LENGTH = 50;

	private final Long id;
	private final Long userId;
	private final Long experienceId;
	private final String ruleKey;
	private final Instant sentAt;

	private NotificationSend(Long id, Long userId, Long experienceId, String ruleKey, Instant sentAt) {
		this.id = id;
		this.userId = userId;
		this.experienceId = experienceId;
		this.ruleKey = ruleKey;
		this.sentAt = sentAt;
	}

	public static NotificationSend create(Long userId, Long experienceId, String ruleKey) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}
		return new NotificationSend(null, userId, experienceId, validatedRuleKey(ruleKey), null);
	}

	public static NotificationSend restore(
		Long id,
		Long userId,
		Long experienceId,
		String ruleKey,
		Instant sentAt
	) {
		return new NotificationSend(id, userId, experienceId, ruleKey, sentAt);
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getExperienceId() {
		return experienceId;
	}

	public String getRuleKey() {
		return ruleKey;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	private static String validatedRuleKey(String ruleKey) {
		if (ruleKey == null || ruleKey.isBlank()) {
			throw new DomainException("ruleKey is required");
		}
		String trimmed = ruleKey.trim();
		if (trimmed.length() > RULE_KEY_MAX_LENGTH) {
			throw new DomainException("ruleKey must be <= " + RULE_KEY_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NotificationSend that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
