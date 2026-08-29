package com.example.myreviewserver.adapter.inbound.scheduler;

import com.example.myreviewserver.application.notification.DispatchNotificationJobsUseCase;
import com.example.myreviewserver.config.NotificationProperties;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically dispatches notification jobs. Concrete rules are later issues.
 *
 * @Component: Spring 빈.
 * @Scheduled: cron 주기에 맞춰 메서드를 실행. zone으로 cron 기준 타임존을 지정.
 */
@Component
public class NotificationDispatcherScheduler {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatcherScheduler.class);

	private final DispatchNotificationJobsUseCase dispatchNotificationJobsUseCase;
	private final NotificationProperties notificationProperties;

	public NotificationDispatcherScheduler(
		DispatchNotificationJobsUseCase dispatchNotificationJobsUseCase,
		NotificationProperties notificationProperties
	) {
		this.dispatchNotificationJobsUseCase = dispatchNotificationJobsUseCase;
		this.notificationProperties = notificationProperties;
	}

	@Scheduled(cron = "${app.notification.cron}", zone = "${app.notification.zone}")
	public void dispatch() {
		if (!notificationProperties.isEnabled()) {
			log.debug("Skip notification dispatch: app.notification.enabled=false");
			return;
		}
		LocalDate today = LocalDate.now(notificationProperties.getZoneId());
		int runnerCount = dispatchNotificationJobsUseCase.execute(today);
		log.info("Notification dispatch finished: runners={}, today={}", runnerCount, today);
	}
}
