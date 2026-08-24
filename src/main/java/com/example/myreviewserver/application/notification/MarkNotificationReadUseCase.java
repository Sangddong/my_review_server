package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marks one inbox notification as read for the authenticated owner.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class MarkNotificationReadUseCase {

	private final NotificationRepository notificationRepository;

	public MarkNotificationReadUseCase(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public Notification execute(Long userId, Long notificationId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (notificationId == null) {
			throw new DomainException("notificationId is required");
		}

		Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
			.orElseThrow(() -> new DomainException("Notification not found"));
		if (notification.isRead()) {
			return notification;
		}
		notification.markRead();
		return notificationRepository.save(notification);
	}
}
