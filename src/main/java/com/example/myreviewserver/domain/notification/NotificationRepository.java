package com.example.myreviewserver.domain.notification;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for user-facing notifications.
 */
public interface NotificationRepository {

	Notification save(Notification notification);

	void saveAll(List<Notification> notificationList);

	Optional<Notification> findByIdAndUserId(Long id, Long userId);

	List<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

	int markAllUnreadAsReadByUserId(Long userId);

	long countUnreadByUserId(Long userId);

	void deleteByIdAndUserId(Long id, Long userId);
}
