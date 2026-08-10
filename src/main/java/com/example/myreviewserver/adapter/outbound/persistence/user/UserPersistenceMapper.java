package com.example.myreviewserver.adapter.outbound.persistence.user;

import com.example.myreviewserver.domain.user.User;

final class UserPersistenceMapper {

	private UserPersistenceMapper() {
	}

	static UserJpaEntity toNewEntity(User user) {
		UserJpaEntity entity = new UserJpaEntity();
		entity.setEmail(user.getEmail());
		entity.setNickname(user.getNickname());
		entity.setIsDeleted(user.getIsDeleted());
		entity.setDeletedAt(user.getDeletedAt());
		entity.setLastLoginAt(user.getLastLoginAt());
		return entity;
	}

	static void copyToEntity(User user, UserJpaEntity entity) {
		entity.setEmail(user.getEmail());
		entity.setNickname(user.getNickname());
		entity.setIsDeleted(user.getIsDeleted());
		entity.setDeletedAt(user.getDeletedAt());
		entity.setLastLoginAt(user.getLastLoginAt());
	}

	static User toDomain(UserJpaEntity entity) {
		return User.restore(
			entity.getId(),
			entity.getEmail(),
			entity.getNickname(),
			entity.getIsDeleted(),
			entity.getDeletedAt(),
			entity.getLastLoginAt(),
			entity.getCreatedAt()
		);
	}
}
