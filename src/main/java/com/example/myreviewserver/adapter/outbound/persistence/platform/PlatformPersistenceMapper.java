package com.example.myreviewserver.adapter.outbound.persistence.platform;

import com.example.myreviewserver.domain.platform.Platform;

final class PlatformPersistenceMapper {

	private PlatformPersistenceMapper() {
	}

	static PlatformJpaEntity toEntity(Platform platform) {
		PlatformJpaEntity entity = new PlatformJpaEntity();
		entity.setId(platform.getId());
		entity.setUserId(platform.getUserId());
		entity.setName(platform.getName());
		entity.setColor(platform.getColor());
		entity.setSortOrder(platform.getSortOrder());
		entity.setIsDeleted(platform.getIsDeleted());
		return entity;
	}

	static void copyToEntity(Platform platform, PlatformJpaEntity entity) {
		entity.setName(platform.getName());
		entity.setColor(platform.getColor());
		entity.setSortOrder(platform.getSortOrder());
		entity.setIsDeleted(platform.getIsDeleted());
	}

	static Platform toDomain(PlatformJpaEntity entity) {
		return Platform.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getName(),
			entity.getColor(),
			entity.getSortOrder(),
			entity.getIsDeleted(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
