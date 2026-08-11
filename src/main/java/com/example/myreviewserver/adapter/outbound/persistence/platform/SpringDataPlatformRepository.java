package com.example.myreviewserver.adapter.outbound.persistence.platform;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for PlatformJpaEntity.
 */
public interface SpringDataPlatformRepository extends JpaRepository<PlatformJpaEntity, Long> {

	Optional<PlatformJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<PlatformJpaEntity> findByUserIdAndIsDeletedIsNullOrderBySortOrderAscIdAsc(Long userId);
}
