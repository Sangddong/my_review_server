package com.example.myreviewserver.adapter.outbound.persistence.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ExperiencePersistenceMapper {

	private ExperiencePersistenceMapper() {
	}

	static ExperienceJpaEntity toNewEntity(Experience experience) {
		ExperienceJpaEntity entity = new ExperienceJpaEntity();
		copyToEntity(experience, entity);
		entity.setUserId(experience.getUserId());
		return entity;
	}

	static void copyToEntity(Experience experience, ExperienceJpaEntity entity) {
		entity.setName(experience.getName());
		entity.setExperienceType(experience.getExperienceType());
		entity.setReservationDate(experience.getReservationDate());
		entity.setReservationTime(experience.getReservationTime());
		entity.setReviewDeadline(experience.getReviewDeadline());
		entity.setIsReviewSubmitted(experience.getIsReviewSubmitted());
		entity.setDetailLink(experience.getDetailLink());
	}

	static ExperiencePlatformJpaEntity toPlatformEntity(Long experienceId, ExperiencePlatform platform) {
		return new ExperiencePlatformJpaEntity(
			new ExperiencePlatformId(experienceId, platform.getPlatformId()),
			platform.isRequired() ? 1 : null
		);
	}

	static ExperienceRegisteredPlatformJpaEntity toRegisteredEntity(Long experienceId, Long platformId) {
		return new ExperienceRegisteredPlatformJpaEntity(
			new ExperiencePlatformId(experienceId, platformId),
			Instant.now()
		);
	}

	/**
	 * Converts entity to domain without loading platform links.
	 * Use only when platform data is not needed (e.g. notification queries).
	 */
	static Experience toDomainMinimal(ExperienceJpaEntity entity) {
		return Experience.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getName(),
			entity.getExperienceType(),
			entity.getReservationDate(),
			entity.getReservationTime(),
			entity.getReviewDeadline(),
			entity.getIsReviewSubmitted(),
			entity.getDetailLink(),
			List.of(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}

	static Experience toDomain(
		ExperienceJpaEntity entity,
		List<ExperiencePlatformJpaEntity> platformRows,
		List<ExperienceRegisteredPlatformJpaEntity> registeredRows
	) {
		Set<Long> registeredIds = new HashSet<>();
		for (ExperienceRegisteredPlatformJpaEntity row : registeredRows) {
			registeredIds.add(row.getId().getPlatformId());
		}

		List<ExperiencePlatform> platformList = new ArrayList<>();
		for (ExperiencePlatformJpaEntity row : platformRows) {
			Long platformId = row.getId().getPlatformId();
			boolean required = row.getIsRequired() != null;
			platformList.add(ExperiencePlatform.of(platformId, required, registeredIds.contains(platformId)));
		}

		return Experience.restore(
			entity.getId(),
			entity.getUserId(),
			entity.getName(),
			entity.getExperienceType(),
			entity.getReservationDate(),
			entity.getReservationTime(),
			entity.getReviewDeadline(),
			entity.getIsReviewSubmitted(),
			entity.getDetailLink(),
			platformList,
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
