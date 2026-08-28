package com.example.myreviewserver.adapter.outbound.persistence.experience;

import java.time.LocalDate;
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

	@Query("""
		select e from ExperienceJpaEntity e
		where e.isReviewSubmitted is null
		  and e.reviewDeadline between :from and :to
		order by e.reviewDeadline asc, e.id asc
		""")
	List<ExperienceJpaEntity> findUnsubmittedByReviewDeadlineBetween(
		@Param("from") LocalDate from,
		@Param("to") LocalDate to
	);

	@Query("""
		select e from ExperienceJpaEntity e
		where e.isReviewSubmitted is null
		  and e.reviewDeadline < :before
		order by e.reviewDeadline asc, e.id asc
		""")
	List<ExperienceJpaEntity> findUnsubmittedByReviewDeadlineBefore(
		@Param("before") LocalDate before
	);

	@Query("""
		select e from ExperienceJpaEntity e
		where e.reservationDate = :reservationDate
		order by
		  case when e.reservationTime is null then 1 else 0 end,
		  e.reservationTime asc,
		  e.id asc
		""")
	List<ExperienceJpaEntity> findByReservationDate(@Param("reservationDate") LocalDate reservationDate);

	@Query("select e.id from ExperienceJpaEntity e where e.userId in :userIds")
	List<Long> findIdsByUserIdIn(@Param("userIds") List<Long> userIds);

	@Query("select e.id from ExperienceJpaEntity e where e.userId = :userId and e.id in :idList")
	List<Long> findIdsByUserIdAndIdIn(@Param("userId") Long userId, @Param("idList") List<Long> idList);

	long deleteByUserIdIn(List<Long> userIds);

	int deleteByUserIdAndIdIn(Long userId, List<Long> idList);
}
