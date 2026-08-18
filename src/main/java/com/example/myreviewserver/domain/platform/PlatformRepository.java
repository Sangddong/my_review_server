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

	/**
	 * Counts active platforms owned by the user whose ids are in the given list.
	 */
	long countActiveByUserIdAndIdIn(Long userId, List<Long> platformIdList);

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

	/**
	 * Sets sortOrder of active platforms in one UPDATE (CASE WHEN).
	 * orderedIds index becomes sortOrder. Returns false when updated row count mismatches.
	 */
	boolean reorderActiveByUserId(Long userId, List<Long> orderedIds);

	void deleteAllByUserIdIn(List<Long> userIdList);
}
