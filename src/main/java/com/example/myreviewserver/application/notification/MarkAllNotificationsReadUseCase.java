package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marks all unread inbox notifications as read for the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class MarkAllNotificationsReadUseCase {

	private final NotificationRepository notificationRepository;

	public MarkAllNotificationsReadUseCase(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public void execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		notificationRepository.markAllUnreadAsReadByUserId(userId);
	}
}
