package com.example.myreviewserver.adapter.outbound.persistence.notification;

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

	Optional<NotificationJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update NotificationJpaEntity n
		set n.isRead = 1
		where n.userId = :userId
		  and n.isRead is null
		""")
	int markAllUnreadAsReadByUserId(@Param("userId") Long userId);

	long countByUserIdAndIsReadIsNull(Long userId);

	long deleteByIdAndUserId(Long id, Long userId);
}
