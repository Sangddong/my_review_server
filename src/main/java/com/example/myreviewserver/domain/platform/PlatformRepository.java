package com.example.myreviewserver.domain.platform;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for Platform aggregate.
 * Implemented by adapter.outbound.persistence.
 */
public interface PlatformRepository {

	Platform save(Platform platform);

	Optional<Platform> findById(Long id);

	Optional<Platform> findByIdAndUserId(Long id, Long userId);

	Optional<Platform> findActiveByIdAndUserId(Long id, Long userId);

	List<Platform> findActiveByUserIdOrderBySortOrderAscIdAsc(Long userId);

	boolean existsActiveByUserIdAndNameExcludingId(Long userId, String name, Long excludeId);

	/**
	 * Updates name and/or color of an active platform.
	 * Null name or color means leave that column unchanged.
	 * Returns empty when no active row matched.
	 */
	Optional<Platform> updateActiveByIdAndUserId(Long id, Long userId, String name, String color);

	/**
	 * Soft-deletes an active platform. Returns false when no active row matched.
	 */
	boolean softDeleteActiveByIdAndUserId(Long id, Long userId);
}
