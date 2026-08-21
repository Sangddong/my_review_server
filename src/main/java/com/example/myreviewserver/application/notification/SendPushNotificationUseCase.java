package com.example.myreviewserver.application.notification;

import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.notification.NotificationSend;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends pushes for notification rules, records duplicate-prevention rows,
 * and saves user inbox notifications.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class SendPushNotificationUseCase {

	private static final Logger log = LoggerFactory.getLogger(SendPushNotificationUseCase.class);

	private final DeviceTokenRepository deviceTokenRepository;
	private final NotificationSendRepository notificationSendRepository;
	private final NotificationRepository notificationRepository;
	private final PushSender pushSender;

	public SendPushNotificationUseCase(
		DeviceTokenRepository deviceTokenRepository,
		NotificationSendRepository notificationSendRepository,
		NotificationRepository notificationRepository,
		PushSender pushSender
	) {
		this.deviceTokenRepository = deviceTokenRepository;
		this.notificationSendRepository = notificationSendRepository;
		this.notificationRepository = notificationRepository;
		this.pushSender = pushSender;
	}

	public int execute(List<NotificationDispatchCommand> commandList) {
		if (commandList == null || commandList.isEmpty()) {
			return 0;
		}

		List<Long> experienceIdList = new ArrayList<>();
		List<String> ruleKeyList = new ArrayList<>();
		for (NotificationDispatchCommand command : commandList) {
			validate(command);
			experienceIdList.add(command.experienceId());
			ruleKeyList.add(command.ruleKey().trim());
		}

		List<NotificationSend> existing = notificationSendRepository.findByExperienceIdInAndRuleKeyIn(
			experienceIdList,
			ruleKeyList
		);
		Set<String> alreadySentKeys = new HashSet<>();
		for (NotificationSend send : existing) {
			alreadySentKeys.add(pairKey(send.getExperienceId(), send.getRuleKey()));
		}

		List<NotificationDispatchCommand> pending = new ArrayList<>();
		List<Long> userIdList = new ArrayList<>();
		Set<String> pendingKeys = new HashSet<>();
		for (NotificationDispatchCommand command : commandList) {
			String key = pairKey(command.experienceId(), command.ruleKey().trim());
			if (alreadySentKeys.contains(key) || !pendingKeys.add(key)) {
				continue;
			}
			pending.add(command);
			userIdList.add(command.userId());
		}
		if (pending.isEmpty()) {
			log.info("Skip push: all {} commands already sent", commandList.size());
			return 0;
		}

		List<DeviceToken> tokenList = deviceTokenRepository.findAllByUserIdIn(userIdList);
		Map<Long, List<String>> tokensByUserId = new HashMap<>();
		for (DeviceToken deviceToken : tokenList) {
			tokensByUserId.computeIfAbsent(deviceToken.getUserId(), ignored -> new ArrayList<>())
				.add(deviceToken.getToken());
		}

		record PendingDelivery(List<String> tokens, PushMessage message) {
		}

		List<NotificationSend> sendRecords = new ArrayList<>();
		List<Notification> inboxRecords = new ArrayList<>();
		List<PendingDelivery> deliveries = new ArrayList<>();
		for (NotificationDispatchCommand command : pending) {
			List<String> userTokens = tokensByUserId.getOrDefault(command.userId(), List.of());
			if (userTokens.isEmpty()) {
				log.info(
					"Skip push: no device tokens userId={} experienceId={} ruleKey={}",
					command.userId(),
					command.experienceId(),
					command.ruleKey()
				);
				continue;
			}
			String title = command.title().trim();
			String body = command.body().trim();
			sendRecords.add(NotificationSend.create(command.userId(), command.experienceId(), command.ruleKey()));
			inboxRecords.add(Notification.create(
				command.userId(),
				command.experienceId(),
				command.ruleKey(),
				title,
				body
			));
			deliveries.add(new PendingDelivery(userTokens, new PushMessage(title, body)));
		}
		if (sendRecords.isEmpty()) {
			return 0;
		}
		notificationSendRepository.saveAll(sendRecords);
		notificationRepository.saveAll(inboxRecords);
		for (PendingDelivery delivery : deliveries) {
			for (String token : delivery.tokens()) {
				pushSender.send(token, delivery.message());
			}
		}
		log.info("Recorded {} notification sends and inbox rows", sendRecords.size());
		return sendRecords.size();
	}

	private static void validate(NotificationDispatchCommand command) {
		if (command == null) {
			throw new DomainException("command is required");
		}
		if (command.userId() == null) {
			throw new DomainException("userId is required");
		}
		if (command.experienceId() == null) {
			throw new DomainException("experienceId is required");
		}
		if (command.ruleKey() == null || command.ruleKey().isBlank()) {
			throw new DomainException("ruleKey is required");
		}
		if (command.title() == null || command.title().isBlank()) {
			throw new DomainException("title is required");
		}
		if (command.body() == null || command.body().isBlank()) {
			throw new DomainException("body is required");
		}
	}

	private static String pairKey(Long experienceId, String ruleKey) {
		return experienceId + ":" + ruleKey;
	}
}
