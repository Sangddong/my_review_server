package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Soft-deletes inbox notifications owned by the authenticated user.
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

	public void execute(Long userId, List<Long> idList) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (idList == null || idList.isEmpty()) {
			throw new DomainException("idList is required");
		}
		if (idList.stream().anyMatch(id -> id == null)) {
			throw new DomainException("idList must not contain null");
		}

		int deleted = notificationRepository.softDeleteByUserIdAndIdIn(userId, idList);
		if (deleted == 0) {
			throw new DomainException("Notification not found");
		}
	}
}
