package com.example.myreviewserver.application.notification;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Periodically invokes registered notification job runners. Follow-up issues add runners.
 *
 * @Service: 서비스 빈.
 */
@Service
public class DispatchNotificationJobsUseCase {

	private static final Logger log = LoggerFactory.getLogger(DispatchNotificationJobsUseCase.class);

	private final List<NotificationJobRunner> runners;

	public DispatchNotificationJobsUseCase(List<NotificationJobRunner> runners) {
		this.runners = runners;
	}

	public int execute(Instant now) {
		Instant runAt = now != null ? now : Instant.now();
		log.info("Dispatching {} notification job runners at {}", runners.size(), runAt);
		for (NotificationJobRunner runner : runners) {
			runner.run(runAt);
		}
		return runners.size();
	}
}
