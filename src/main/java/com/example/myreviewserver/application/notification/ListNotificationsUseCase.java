package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the authenticated user's inbox notifications, newest first.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션 (readOnly = 조회만).
 */
@Service
@Transactional(readOnly = true)
public class ListNotificationsUseCase {

	private final NotificationRepository notificationRepository;

	public ListNotificationsUseCase(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public List<Notification> execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		return notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId);
	}
}
