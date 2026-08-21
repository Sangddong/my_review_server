package com.example.myreviewserver.adapter.outbound.persistence.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

	Optional<NotificationJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

	long countByUserIdAndIsReadIsNull(Long userId);

	long deleteByIdAndUserId(Long id, Long userId);
}
