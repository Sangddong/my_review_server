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

	List<Platform> findActiveByUserIdOrderBySortOrderAscIdAsc(Long userId);
}
