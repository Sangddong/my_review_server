package com.example.myreviewserver.adapter.inbound.scheduler;

import com.example.myreviewserver.application.platform.PurgeDeletedPlatformsUseCase;
import com.example.myreviewserver.config.PlatformPurgeProperties;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically hard-deletes soft-deleted platforms past the retention window.
 *
 * @Component: Spring 빈.
 * @Scheduled: cron 주기에 맞춰 메서드를 실행.
 */
@Component
public class PurgeDeletedPlatformsScheduler {

	private static final Logger log = LoggerFactory.getLogger(PurgeDeletedPlatformsScheduler.class);

	private final PurgeDeletedPlatformsUseCase purgeDeletedPlatformsUseCase;
	private final PlatformPurgeProperties platformPurgeProperties;

	public PurgeDeletedPlatformsScheduler(
		PurgeDeletedPlatformsUseCase purgeDeletedPlatformsUseCase,
		PlatformPurgeProperties platformPurgeProperties
	) {
		this.purgeDeletedPlatformsUseCase = purgeDeletedPlatformsUseCase;
		this.platformPurgeProperties = platformPurgeProperties;
	}

	@Scheduled(cron = "${app.platform.purge.cron}")
	public void purge() {
		int afterMonths = platformPurgeProperties.getAfterMonths();
		if (afterMonths < 1) {
			log.warn(
				"Skip platform purge: app.platform.purge.after-months must be >= 1 (was {})",
				afterMonths
			);
			return;
		}
		Instant cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(afterMonths).toInstant();
		int deleted = purgeDeletedPlatformsUseCase.execute(cutoff);
		log.info("Platform purge job finished: deleted={}, cutoff={}", deleted, cutoff);
	}
}
