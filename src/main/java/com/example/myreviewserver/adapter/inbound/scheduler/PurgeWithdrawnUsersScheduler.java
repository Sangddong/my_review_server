package com.example.myreviewserver.adapter.inbound.scheduler;

import com.example.myreviewserver.application.user.PurgeWithdrawnUsersUseCase;
import com.example.myreviewserver.config.UserPurgeProperties;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically hard-deletes withdrawn users past the retention window.
 *
 * @Component: Spring 빈.
 * @Scheduled: cron 주기에 맞춰 메서드를 실행.
 */
@Component
public class PurgeWithdrawnUsersScheduler {

	private static final Logger log = LoggerFactory.getLogger(PurgeWithdrawnUsersScheduler.class);

	private final PurgeWithdrawnUsersUseCase purgeWithdrawnUsersUseCase;
	private final UserPurgeProperties userPurgeProperties;

	public PurgeWithdrawnUsersScheduler(
		PurgeWithdrawnUsersUseCase purgeWithdrawnUsersUseCase,
		UserPurgeProperties userPurgeProperties
	) {
		this.purgeWithdrawnUsersUseCase = purgeWithdrawnUsersUseCase;
		this.userPurgeProperties = userPurgeProperties;
	}

	@Scheduled(cron = "${app.user.purge.cron}")
	public void purge() {
		int afterMonths = userPurgeProperties.getAfterMonths();
		if (afterMonths < 1) {
			log.warn("Skip user purge: app.user.purge.after-months must be >= 1 (was {})", afterMonths);
			return;
		}
		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(afterMonths).toInstant();
		int deleted = purgeWithdrawnUsersUseCase.execute(cutoff);
		log.info("User purge job finished: deleted={}, cutoff={}", deleted, cutoff);
	}
}
