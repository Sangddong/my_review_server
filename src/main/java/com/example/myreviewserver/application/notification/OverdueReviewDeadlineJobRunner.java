package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Notification rule: pushes when review_deadline has passed and not yet submitted.
 * ruleKey = "OVERDUE"
 *
 * @Component: Spring 빈으로 등록되어 NotificationJobRunner 목록에 자동 포함됨.
 */
@Component
public class OverdueReviewDeadlineJobRunner implements NotificationJobRunner {

	static final String RULE_KEY = "OVERDUE";

	private static final Logger log = LoggerFactory.getLogger(OverdueReviewDeadlineJobRunner.class);

	private final ExperienceRepository experienceRepository;
	private final SendPushNotificationUseCase sendPushNotificationUseCase;

	public OverdueReviewDeadlineJobRunner(
		ExperienceRepository experienceRepository,
		SendPushNotificationUseCase sendPushNotificationUseCase
	) {
		this.experienceRepository = experienceRepository;
		this.sendPushNotificationUseCase = sendPushNotificationUseCase;
	}

	@Override
	public void run(LocalDate today) {
		List<Experience> experiences = experienceRepository.findUnsubmittedByReviewDeadlineBefore(today);
		log.info("OVERDUE rule: found {} experiences with deadline before {}", experiences.size(), today);

		if (experiences.isEmpty()) {
			return;
		}

		List<NotificationDispatchCommand> commandList = new ArrayList<>();
		for (Experience experience : experiences) {
			commandList.add(new NotificationDispatchCommand(
				experience.getUserId(),
				experience.getId(),
				RULE_KEY,
				NotificationCopy.overdueTitle(experience.getName()),
				NotificationCopy.overdueBody()
			));
		}
		int sent = sendPushNotificationUseCase.execute(commandList);
		log.info("OVERDUE rule: sent={} skipped={}", sent, commandList.size() - sent);
	}
}
