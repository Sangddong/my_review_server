package com.example.myreviewserver.adapter.outbound.persistence.platform;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for PlatformJpaEntity.
 *
 * @Query: custom JPQL when method-name derivation is not enough.
 */
public interface SpringDataPlatformRepository extends JpaRepository<PlatformJpaEntity, Long> {

	Optional<PlatformJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<PlatformJpaEntity> findByUserIdAndIsDeletedIsNullOrderBySortOrderAscIdAsc(Long userId);

	@Query("""
		select coalesce(max(p.sortOrder), -1) + 1
		from PlatformJpaEntity p
		where p.userId = :userId
		""")
	int findNextSortOrder(@Param("userId") Long userId);
}
