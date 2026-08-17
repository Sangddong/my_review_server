package com.example.myreviewserver.adapter.outbound.persistence.experience;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for ExperienceJpaEntity.
 *
 * @Query: custom JPQL when method-name derivation is not enough.
 * @Param: JPQL의 :이름 자리에 메서드 인자를 넣음.
 */
public interface SpringDataExperienceRepository extends JpaRepository<ExperienceJpaEntity, Long> {

	Optional<ExperienceJpaEntity> findByIdAndUserId(Long id, Long userId);

	@Query("""
		select e from ExperienceJpaEntity e
		where e.userId = :userId
		  and e.isReviewSubmitted is null
		order by
		  case when e.reservationDate is null then 1 else 0 end,
		  e.reservationDate asc,
		  case when e.reservationTime is null then 1 else 0 end,
		  e.reservationTime asc,
		  e.id asc
		""")
	List<ExperienceJpaEntity> findUpcomingByUserIdOrderByReservationAscIdAsc(@Param("userId") Long userId);

	@Query("""
		select e from ExperienceJpaEntity e
		where e.userId = :userId
		  and e.isReviewSubmitted = 1
		order by
		  case when e.reservationDate is null then 1 else 0 end,
		  e.reservationDate asc,
		  case when e.reservationTime is null then 1 else 0 end,
		  e.reservationTime asc,
		  e.id asc
		""")
	List<ExperienceJpaEntity> findCompletedByUserIdOrderByReservationAscIdAsc(@Param("userId") Long userId);

	List<ExperienceJpaEntity> findByUserId(Long userId);
}
