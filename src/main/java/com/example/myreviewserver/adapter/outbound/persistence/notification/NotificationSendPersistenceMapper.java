package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationSend;
import java.time.Instant;

final class NotificationSendPersistenceMapper {

	private NotificationSendPersistenceMapper() {
	}

	static NotificationSendJpaEntity toNewEntity(NotificationSend domain) {
		NotificationSendJpaEntity entity = new NotificationSendJpaEntity();
		entity.setUserId(domain.getUserId());
		entity.setExperienceId(domain.getExperienceId());
		entity.setRuleKey(domain.getRuleKey());
		entity.setSentAt(domain.getSentAt() != null ? domain.getSentAt() : Instant.now());
		return entity;
	}

	static NotificationSend toDomain(NotificationSendJpaEntity entity) {
		return NotificationSend.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getExperienceId(),
			entity.getRuleKey(),
			entity.getSentAt()
		);
	}
}
