package com.example.myreviewserver.adapter.outbound.persistence.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter implementing the domain PlatformRepository port.
 *
 * @Repository: Spring stereotype for persistence components (also enables exception translation).
 * @Transactional: wraps methods in a DB transaction.
 */
@Repository
@Transactional
public class PlatformRepositoryAdapter implements PlatformRepository {

	private final SpringDataPlatformRepository springDataPlatformRepository;
	private final EntityManager entityManager;

	public PlatformRepositoryAdapter(
		SpringDataPlatformRepository springDataPlatformRepository,
		EntityManager entityManager
	) {
		this.springDataPlatformRepository = springDataPlatformRepository;
		this.entityManager = entityManager;
	}

	@Override
	public Platform save(Platform platform) {
		PlatformJpaEntity entity;
		if (platform.getId() == null) {
			entity = PlatformPersistenceMapper.toEntity(platform);
		}
		else {
			entity = springDataPlatformRepository
				.findByIdAndUserId(platform.getId(), platform.getUserId())
				.orElseThrow(() -> new DomainException("Platform not found"));
			PlatformPersistenceMapper.copyToEntity(platform, entity);
		}

		PlatformJpaEntity saved = springDataPlatformRepository.saveAndFlush(entity);
		entityManager.refresh(saved);
		return PlatformPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Platform> findById(Long id) {
		return springDataPlatformRepository.findById(id).map(PlatformPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Platform> findByIdAndUserId(Long id, Long userId) {
		return springDataPlatformRepository.findByIdAndUserId(id, userId)
			.map(PlatformPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Platform> findActiveByUserIdOrderBySortOrderAscIdAsc(Long userId) {
		return springDataPlatformRepository
			.findByUserIdAndIsDeletedIsNullOrderBySortOrderAscIdAsc(userId)
			.stream()
			.map(PlatformPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public int findNextSortOrder(Long userId) {
		return springDataPlatformRepository.findNextSortOrder(userId);
	}
}
