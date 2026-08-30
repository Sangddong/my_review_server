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
 * Notification rule: pushes when review_deadline is exactly 3 days away and not yet submitted.
 * ruleKey = "D3"
 *
 * @Component: Spring 빈으로 등록되어 NotificationJobRunner 목록에 자동 포함됨.
 */
@Component
public class D3ReviewDeadlineJobRunner implements NotificationJobRunner {

	static final String RULE_KEY = "D3";

	private static final Logger log = LoggerFactory.getLogger(D3ReviewDeadlineJobRunner.class);

	private final ExperienceRepository experienceRepository;
	private final SendPushNotificationUseCase sendPushNotificationUseCase;

	public D3ReviewDeadlineJobRunner(
		ExperienceRepository experienceRepository,
		SendPushNotificationUseCase sendPushNotificationUseCase
	) {
		this.experienceRepository = experienceRepository;
		this.sendPushNotificationUseCase = sendPushNotificationUseCase;
	}

	@Override
	public void run(LocalDate today) {
		LocalDate target = today.plusDays(3);

		List<Experience> experiences = experienceRepository.findUnsubmittedByReviewDeadlineBetween(target, target);
		log.info("D3 rule: found {} experiences with deadline={}", experiences.size(), target);

		if (experiences.isEmpty()) {
			return;
		}

		List<NotificationDispatchCommand> commandList = new ArrayList<>();
		for (Experience experience : experiences) {
			commandList.add(new NotificationDispatchCommand(
				experience.getUserId(),
				experience.getId(),
				RULE_KEY,
				NotificationCopy.d3Title(experience.getName()),
				NotificationCopy.d3Body()
			));
		}
		int sent = sendPushNotificationUseCase.execute(commandList);
		log.info("D3 rule: sent={} skipped={}", sent, commandList.size() - sent);
	}
}
