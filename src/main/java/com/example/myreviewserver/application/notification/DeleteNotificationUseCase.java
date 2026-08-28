package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes one inbox notification owned by the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class DeleteNotificationUseCase {

	private final NotificationRepository notificationRepository;

	public DeleteNotificationUseCase(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public void execute(Long userId, Long notificationId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (notificationId == null) {
			throw new DomainException("notificationId is required");
		}

		boolean deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
		if (!deleted) {
			throw new DomainException("Notification not found");
		}
	}
}
