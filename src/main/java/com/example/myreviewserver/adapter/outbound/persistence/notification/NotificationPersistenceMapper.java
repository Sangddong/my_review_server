package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.Notification;
import java.time.Instant;

final class NotificationPersistenceMapper {

	private NotificationPersistenceMapper() {
	}

	static NotificationJpaEntity toNewEntity(Notification domain) {
		NotificationJpaEntity entity = new NotificationJpaEntity();
		copyToEntity(domain, entity);
		entity.setUserId(domain.getUserId());
		entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
		return entity;
	}

	static void copyToEntity(Notification domain, NotificationJpaEntity entity) {
		entity.setExperienceId(domain.getExperienceId());
		entity.setRuleKey(domain.getRuleKey());
		entity.setTitle(domain.getTitle());
		entity.setBody(domain.getBody());
		entity.setIsRead(domain.getIsRead());
	}

	static Notification toDomain(NotificationJpaEntity entity) {
		return Notification.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getExperienceId(),
			entity.getRuleKey(),
			entity.getTitle(),
			entity.getBody(),
			entity.getIsRead(),
			entity.getCreatedAt()
		);
	}
}
