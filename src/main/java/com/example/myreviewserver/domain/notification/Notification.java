package com.example.myreviewserver.domain.notification;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

/**
 * User-facing notification inbox item (app 알림 탭).
 * is_read: null = unread, 1 = read.
 * is_deleted: null = active, 1 = soft-deleted.
 */
public class Notification {

	private static final int RULE_KEY_MAX_LENGTH = 50;
	private static final int TITLE_MAX_LENGTH = 200;
	private static final int BODY_MAX_LENGTH = 500;

	private final Long id;
	private final Long userId;
	private final Long experienceId;
	private final String ruleKey;
	private final String title;
	private final String body;
	private Integer isRead;
	private Integer isDeleted;
	private Instant deletedAt;
	private final Instant createdAt;

	private Notification(
		Long id,
		Long userId,
		Long experienceId,
		String ruleKey,
		String title,
		String body,
		Integer isRead,
		Integer isDeleted,
		Instant deletedAt,
		Instant createdAt
	) {
		this.id = id;
		this.userId = userId;
		this.experienceId = experienceId;
		this.ruleKey = ruleKey;
		this.title = title;
		this.body = body;
		this.isRead = isRead;
		this.isDeleted = isDeleted;
		this.deletedAt = deletedAt;
		this.createdAt = createdAt;
	}

	public static Notification create(
		Long userId,
		Long experienceId,
		String ruleKey,
		String title,
		String body
	) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}
		return new Notification(
			null,
			userId,
			experienceId,
			validatedRuleKey(ruleKey),
			validatedTitle(title),
			validatedBody(body),
			null,
			null,
			null,
			null
		);
	}

	public static Notification restore(
		Long id,
		Long userId,
		Long experienceId,
		String ruleKey,
		String title,
		String body,
		Integer isRead,
		Integer isDeleted,
		Instant deletedAt,
		Instant createdAt
	) {
		return new Notification(
			id,
			userId,
			experienceId,
			ruleKey,
			title,
			body,
			isRead,
			isDeleted,
			deletedAt,
			createdAt
		);
	}

	public void markRead() {
		this.isRead = 1;
	}

	public void softDelete(Instant deletedAt) {
		if (deletedAt == null) {
			throw new DomainException("deletedAt is required");
		}
		this.isDeleted = 1;
		this.deletedAt = deletedAt;
	}

	public boolean isRead() {
		return isRead != null;
	}

	public boolean isDeleted() {
		return isDeleted != null;
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

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public Integer getIsRead() {
		return isRead;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
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

	private static String validatedTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new DomainException("title is required");
		}
		String trimmed = title.trim();
		if (trimmed.length() > TITLE_MAX_LENGTH) {
			throw new DomainException("title must be <= " + TITLE_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	private static String validatedBody(String body) {
		if (body == null || body.isBlank()) {
			throw new DomainException("body is required");
		}
		String trimmed = body.trim();
		if (trimmed.length() > BODY_MAX_LENGTH) {
			throw new DomainException("body must be <= " + BODY_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Notification that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
