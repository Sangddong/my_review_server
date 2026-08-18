package com.example.myreviewserver.adapter.outbound.persistence.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * JPA mapping for notification_sends table.
 *
 * @Entity: JPA가 관리하는 테이블 행.
 * @Table: DB 테이블 이름과 유니크 제약.
 */
@Entity
@Table(
	name = "notification_sends",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_notification_sends_experience_rule",
		columnNames = {"experience_id", "rule_key"}
	)
)
public class NotificationSendJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "experience_id", nullable = false)
	private Long experienceId;

	@Column(name = "rule_key", nullable = false, length = 50)
	private String ruleKey;

	@Column(name = "sent_at", nullable = false, updatable = false)
	private Instant sentAt;

	protected NotificationSendJpaEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getExperienceId() {
		return experienceId;
	}

	public void setExperienceId(Long experienceId) {
		this.experienceId = experienceId;
	}

	public String getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(String ruleKey) {
		this.ruleKey = ruleKey;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(Instant sentAt) {
		this.sentAt = sentAt;
	}
}
