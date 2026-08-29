package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
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

	public int execute(LocalDate today) {
		if (today == null) {
			throw new DomainException("today is required");
		}
		log.info("Dispatching {} notification job runners for {}", runners.size(), today);
		for (NotificationJobRunner runner : runners) {
			runner.run(today);
		}
		return runners.size();
	}
}
