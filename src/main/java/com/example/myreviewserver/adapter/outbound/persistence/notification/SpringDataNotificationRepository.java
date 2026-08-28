package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for NotificationJpaEntity.
 *
 * @Query: JPQL 직접 작성.
 * @Modifying: UPDATE/DELETE 쿼리임을 Spring Data에 알림.
 * @Param: JPQL의 :이름 자리에 메서드 인자를 넣음.
 */
public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

	@Query("""
		select n from NotificationJpaEntity n
		where n.id = :id
		  and n.userId = :userId
		  and n.isDeleted is null
		""")
	Optional<NotificationJpaEntity> findActiveByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	@Query("""
		select n from NotificationJpaEntity n
		where n.userId = :userId
		  and n.isDeleted is null
		order by n.createdAt desc, n.id desc
		""")
	List<NotificationJpaEntity> findActiveByUserIdOrderByCreatedAtDescIdDesc(@Param("userId") Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update NotificationJpaEntity n
		set n.isRead = 1
		where n.userId = :userId
		  and n.isRead is null
		  and n.isDeleted is null
		""")
	int markAllUnreadAsReadByUserId(@Param("userId") Long userId);

	@Query("""
		select count(n) from NotificationJpaEntity n
		where n.userId = :userId
		  and n.isRead is null
		  and n.isDeleted is null
		""")
	long countUnreadByUserId(@Param("userId") Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update NotificationJpaEntity n
		set n.isDeleted = 1, n.deletedAt = :deletedAt
		where n.userId = :userId
		  and n.id in :idList
		  and n.isDeleted is null
		""")
	int softDeleteByUserIdAndIdIn(
		@Param("userId") Long userId,
		@Param("idList") List<Long> idList,
		@Param("deletedAt") Instant deletedAt
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		delete from NotificationJpaEntity n
		where n.isDeleted = 1
		  and n.deletedAt < :cutoff
		""")
	int deleteAllDeletedBefore(@Param("cutoff") Instant cutoff);
}
