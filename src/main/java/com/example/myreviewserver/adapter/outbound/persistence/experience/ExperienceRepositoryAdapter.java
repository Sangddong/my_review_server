package com.example.myreviewserver.adapter.outbound.persistence.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter implementing the domain ExperienceRepository port.
 *
 * @Repository: DB 쪽 컴포넌트.
 * @Transactional: DB 트랜잭션.
 */
@Repository
@Transactional
public class ExperienceRepositoryAdapter implements ExperienceRepository {

	private final SpringDataExperienceRepository experienceRepository;
	private final SpringDataExperiencePlatformRepository platformLinkRepository;
	private final SpringDataExperienceRegisteredPlatformRepository registeredPlatformRepository;
	private final EntityManager entityManager;

	public ExperienceRepositoryAdapter(
		SpringDataExperienceRepository experienceRepository,
		SpringDataExperiencePlatformRepository platformLinkRepository,
		SpringDataExperienceRegisteredPlatformRepository registeredPlatformRepository,
		EntityManager entityManager
	) {
		this.experienceRepository = experienceRepository;
		this.platformLinkRepository = platformLinkRepository;
		this.registeredPlatformRepository = registeredPlatformRepository;
		this.entityManager = entityManager;
	}

	@Override
	public Experience save(Experience experience) {
		ExperienceJpaEntity entity;
		if (experience.getId() == null) {
			entity = ExperiencePersistenceMapper.toNewEntity(experience);
		}
		else {
			entity = experienceRepository
				.findByIdAndUserId(experience.getId(), experience.getUserId())
				.orElseThrow(() -> new DomainException("Experience not found"));
			ExperiencePersistenceMapper.copyToEntity(experience, entity);
		}

		ExperienceJpaEntity saved = experienceRepository.saveAndFlush(entity);
		entityManager.refresh(saved);

		replaceLinks(saved.getId(), experience.getPlatforms());
		return toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Experience> findById(Long id) {
		return experienceRepository.findById(id).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Experience> findByIdAndUserId(Long id, Long userId) {
		return experienceRepository.findByIdAndUserId(id, userId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Experience> findUpcomingByUserIdOrderByReservationAscIdAsc(Long userId) {
		return experienceRepository
			.findUpcomingByUserIdOrderByReservationAscIdAsc(userId)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Experience> findCompletedByUserIdOrderByReservationAscIdAsc(Long userId) {
		return experienceRepository
			.findCompletedByUserIdOrderByReservationAscIdAsc(userId)
			.stream()
			.map(this::toDomain)
			.toList();
	}

	@Override
	public boolean deleteByIdAndUserId(Long id, Long userId) {
		Optional<ExperienceJpaEntity> found = experienceRepository.findByIdAndUserId(id, userId);
		if (found.isEmpty()) {
			return false;
		}
		registeredPlatformRepository.deleteByIdExperienceId(id);
		platformLinkRepository.deleteByIdExperienceId(id);
		experienceRepository.delete(found.get());
		return true;
	}

	private void replaceLinks(Long experienceId, List<ExperiencePlatform> platforms) {
		registeredPlatformRepository.deleteByIdExperienceId(experienceId);
		entityManager.flush();
		platformLinkRepository.deleteByIdExperienceId(experienceId);
		entityManager.flush();

		for (ExperiencePlatform platform : platforms) {
			platformLinkRepository.save(ExperiencePersistenceMapper.toPlatformEntity(experienceId, platform));
		}
		entityManager.flush();

		for (ExperiencePlatform platform : platforms) {
			if (platform.isRegistered()) {
				registeredPlatformRepository.save(
					ExperiencePersistenceMapper.toRegisteredEntity(experienceId, platform.getPlatformId())
				);
			}
		}
		entityManager.flush();
	}

	private Experience toDomain(ExperienceJpaEntity entity) {
		return ExperiencePersistenceMapper.toDomain(
			entity,
			platformLinkRepository.findByIdExperienceId(entity.getId()),
			registeredPlatformRepository.findByIdExperienceId(entity.getId())
		);
	}
}
