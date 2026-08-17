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
	public Optional<Platform> findActiveByIdAndUserId(Long id, Long userId) {
		return springDataPlatformRepository.findByIdAndUserIdAndIsDeletedIsNull(id, userId)
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
	public long countActiveByUserIdAndIdIn(Long userId, List<Long> platformIdList) {
		if (platformIdList == null || platformIdList.isEmpty()) {
			return 0L;
		}
		return springDataPlatformRepository.countByUserIdAndIdInAndIsDeletedIsNull(userId, platformIdList);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsActiveByUserIdAndNameExcludingId(Long userId, String name, Long excludeId) {
		return springDataPlatformRepository
			.existsByUserIdAndNameAndIsDeletedIsNullAndIdNot(userId, name, excludeId);
	}

	@Override
	public Optional<Platform> updateActiveByIdAndUserId(Long id, Long userId, String name, String color) {
		int updated = springDataPlatformRepository.updateActiveByIdAndUserId(id, userId, name, color);
		if (updated == 0) {
			return Optional.empty();
		}
		return findByIdAndUserId(id, userId);
	}

	@Override
	public boolean softDeleteActiveByIdAndUserId(Long id, Long userId) {
		return springDataPlatformRepository.softDeleteActiveByIdAndUserId(id, userId) > 0;
	}

	@Override
	public boolean reorderActiveByUserId(Long userId, List<Long> orderedIds) {
		if (orderedIds.isEmpty()) {
			return true;
		}

		StringBuilder caseExpr = new StringBuilder("CASE p.id ");
		for (int index = 0; index < orderedIds.size(); index++) {
			caseExpr.append("WHEN :id").append(index)
				.append(" THEN :ord").append(index)
				.append(' ');
		}
		caseExpr.append("ELSE p.sortOrder END");

		var query = entityManager.createQuery("""
			update PlatformJpaEntity p
			set p.sortOrder = %s
			where p.userId = :userId
			  and p.isDeleted is null
			  and p.id in :ids
			""".formatted(caseExpr));
		query.setParameter("userId", userId);
		query.setParameter("ids", orderedIds);
		for (int index = 0; index < orderedIds.size(); index++) {
			query.setParameter("id" + index, orderedIds.get(index));
			query.setParameter("ord" + index, index);
		}

		int updated = query.executeUpdate();
		entityManager.clear();
		return updated == orderedIds.size();
	}

	@Override
	public void deleteAllByUserId(Long userId) {
		springDataPlatformRepository.deleteByUserId(userId);
	}
}
