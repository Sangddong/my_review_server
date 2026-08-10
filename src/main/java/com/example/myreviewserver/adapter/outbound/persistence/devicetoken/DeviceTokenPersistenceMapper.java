package com.example.myreviewserver.adapter.outbound.persistence.devicetoken;

import com.example.myreviewserver.domain.devicetoken.DeviceToken;

final class DeviceTokenPersistenceMapper {

	private DeviceTokenPersistenceMapper() {
	}

	static DeviceTokenJpaEntity toNewEntity(DeviceToken domain) {
		DeviceTokenJpaEntity entity = new DeviceTokenJpaEntity();
		entity.setUserId(domain.getUserId());
		entity.setToken(domain.getToken());
		entity.setPlatform(domain.getPlatform());
		return entity;
	}

	static void copyToEntity(DeviceToken domain, DeviceTokenJpaEntity entity) {
		entity.setUserId(domain.getUserId());
		entity.setToken(domain.getToken());
		entity.setPlatform(domain.getPlatform());
	}

	static DeviceToken toDomain(DeviceTokenJpaEntity entity) {
		return DeviceToken.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getToken(),
			entity.getPlatform(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
