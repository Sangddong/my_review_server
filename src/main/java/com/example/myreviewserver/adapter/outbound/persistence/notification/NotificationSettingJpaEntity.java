package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mapping for notification_settings table.
 *
 * @Entity: JPA가 관리하는 테이블 행.
 * @Table: DB 테이블 이름.
 * @Enumerated: enum을 문자열(D3 등)로 저장.
 */
@Entity
@Table(name = "notification_settings")
public class NotificationSettingJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "rule_key", nullable = false, length = 20)
	private NotificationRuleKey ruleKey;

	@Column(name = "is_enabled", nullable = false)
	private boolean enabled;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Instant updatedAt;

	protected NotificationSettingJpaEntity() {
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

	public NotificationRuleKey getRuleKey() {
		return ruleKey;
	}

	public void setRuleKey(NotificationRuleKey ruleKey) {
		this.ruleKey = ruleKey;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
