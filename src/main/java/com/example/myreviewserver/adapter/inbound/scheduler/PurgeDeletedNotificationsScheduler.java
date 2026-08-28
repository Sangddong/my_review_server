package com.example.myreviewserver.adapter.inbound.scheduler;

import com.example.myreviewserver.application.notification.PurgeDeletedNotificationsUseCase;
import com.example.myreviewserver.config.NotificationPurgeProperties;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically hard-deletes soft-deleted notifications past the retention window.
 *
 * @Component: Spring 빈.
 * @Scheduled: cron 주기에 맞춰 메서드를 실행.
 */
@Component
public class PurgeDeletedNotificationsScheduler {

	private static final Logger log = LoggerFactory.getLogger(PurgeDeletedNotificationsScheduler.class);

	private final PurgeDeletedNotificationsUseCase purgeDeletedNotificationsUseCase;
	private final NotificationPurgeProperties notificationPurgeProperties;

	public PurgeDeletedNotificationsScheduler(
		PurgeDeletedNotificationsUseCase purgeDeletedNotificationsUseCase,
		NotificationPurgeProperties notificationPurgeProperties
	) {
		this.purgeDeletedNotificationsUseCase = purgeDeletedNotificationsUseCase;
		this.notificationPurgeProperties = notificationPurgeProperties;
	}

	@Scheduled(cron = "${app.notification.purge.cron}")
	public void purge() {
		int afterMonths = notificationPurgeProperties.getAfterMonths();
		if (afterMonths < 1) {
			log.warn(
				"Skip notification purge: app.notification.purge.after-months must be >= 1 (was {})",
				afterMonths
			);
			return;
		}
		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(afterMonths).toInstant();
		int deleted = purgeDeletedNotificationsUseCase.execute(cutoff);
		log.info("Notification purge job finished: deleted={}, cutoff={}", deleted, cutoff);
	}
}
