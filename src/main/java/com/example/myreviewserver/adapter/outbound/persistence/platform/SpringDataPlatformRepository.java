package com.example.myreviewserver.adapter.outbound.persistence.platform;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for PlatformJpaEntity.
 *
 * @Query: custom JPQL when method-name derivation is not enough.
 * @Modifying: UPDATE/DELETE 쿼리임을 Spring Data에 알림.
 * @Param: JPQL의 :이름 자리에 메서드 인자를 넣음.
 */
public interface SpringDataPlatformRepository extends JpaRepository<PlatformJpaEntity, Long> {

	Optional<PlatformJpaEntity> findByIdAndUserId(Long id, Long userId);

	Optional<PlatformJpaEntity> findByIdAndUserIdAndIsDeletedIsNull(Long id, Long userId);

	List<PlatformJpaEntity> findByUserIdAndIsDeletedIsNullOrderBySortOrderAscIdAsc(Long userId);

	long countByUserIdAndIdInAndIsDeletedIsNull(Long userId, List<Long> ids);

	boolean existsByUserIdAndNameAndIsDeletedIsNullAndIdNot(Long userId, String name, Long id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update PlatformJpaEntity p
		set p.name = coalesce(:name, p.name),
			p.color = coalesce(:color, p.color)
		where p.id = :id
		  and p.userId = :userId
		  and p.isDeleted is null
		  and (
			:name is null
			or not exists (
				select 1 from PlatformJpaEntity other
				where other.userId = :userId
				  and other.name = :name
				  and other.isDeleted is null
				  and other.id <> :id
			)
		  )
		""")
	int updateActiveByIdAndUserId(
		@Param("id") Long id,
		@Param("userId") Long userId,
		@Param("name") String name,
		@Param("color") String color
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update PlatformJpaEntity p
		set p.isDeleted = 1
		where p.id = :id
		  and p.userId = :userId
		  and p.isDeleted is null
		""")
	int softDeleteActiveByIdAndUserId(
		@Param("id") Long id,
		@Param("userId") Long userId
	);

	long deleteByUserId(Long userId);
}
