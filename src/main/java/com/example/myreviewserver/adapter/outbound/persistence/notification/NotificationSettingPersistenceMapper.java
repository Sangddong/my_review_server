package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationSetting;

final class NotificationSettingPersistenceMapper {

	private NotificationSettingPersistenceMapper() {
	}

	static NotificationSettingJpaEntity toNewEntity(NotificationSetting domain) {
		NotificationSettingJpaEntity entity = new NotificationSettingJpaEntity();
		entity.setUserId(domain.getUserId());
		entity.setRuleKey(domain.getRuleKey());
		entity.setEnabled(domain.isEnabled());
		return entity;
	}

	static void copyToEntity(NotificationSetting domain, NotificationSettingJpaEntity entity) {
		entity.setEnabled(domain.isEnabled());
	}

	static NotificationSetting toDomain(NotificationSettingJpaEntity entity) {
		return NotificationSetting.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getRuleKey(),
			entity.isEnabled(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
