package com.example.myreviewserver.domain.notification;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

/**
 * Per-user push preference for one notification rule.
 * A missing row means the rule is enabled.
 */
public class NotificationSetting {

	private final Long id;
	private final Long userId;
	private final NotificationRuleKey ruleKey;
	private boolean enabled;
	private final Instant createdAt;
	private final Instant updatedAt;

	private NotificationSetting(
		Long id,
		Long userId,
		NotificationRuleKey ruleKey,
		boolean enabled,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.ruleKey = ruleKey;
		this.enabled = enabled;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static NotificationSetting create(Long userId, NotificationRuleKey ruleKey, boolean enabled) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (ruleKey == null) {
			throw new DomainException("ruleKey is required");
		}
		return new NotificationSetting(null, userId, ruleKey, enabled, null, null);
	}

	public static NotificationSetting restore(
		Long id,
		Long userId,
		NotificationRuleKey ruleKey,
		boolean enabled,
		Instant createdAt,
		Instant updatedAt
	) {
		return new NotificationSetting(id, userId, ruleKey, enabled, createdAt, updatedAt);
	}

	public void changeEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public NotificationRuleKey getRuleKey() {
		return ruleKey;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NotificationSetting that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
