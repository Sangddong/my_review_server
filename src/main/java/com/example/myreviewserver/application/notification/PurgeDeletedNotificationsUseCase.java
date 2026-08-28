package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes notifications that were soft-deleted before the retention cutoff.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class PurgeDeletedNotificationsUseCase {

	private static final Logger log = LoggerFactory.getLogger(PurgeDeletedNotificationsUseCase.class);

	private final NotificationRepository notificationRepository;

	public PurgeDeletedNotificationsUseCase(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public int execute(Instant cutoff) {
		if (cutoff == null) {
			throw new DomainException("cutoff is required");
		}

		int deleted = notificationRepository.deleteAllDeletedBefore(cutoff);
		log.info("Hard-deleted {} soft-deleted notifications before {}", deleted, cutoff);
		return deleted;
	}
}
