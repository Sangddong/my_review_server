package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationSend;
import com.example.myreviewserver.domain.notification.NotificationSendRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter for notification_sends.
 *
 * @Repository: 영속성 컴포넌트.
 * @Transactional: DB 트랜잭션.
 */
@Repository
@Transactional
public class NotificationSendRepositoryAdapter implements NotificationSendRepository {

	private final SpringDataNotificationSendRepository springDataNotificationSendRepository;

	public NotificationSendRepositoryAdapter(
		SpringDataNotificationSendRepository springDataNotificationSendRepository
	) {
		this.springDataNotificationSendRepository = springDataNotificationSendRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationSend> findByExperienceIdInAndRuleKeyIn(
		List<Long> experienceIdList,
		List<String> ruleKeyList
	) {
		if (experienceIdList == null || experienceIdList.isEmpty()
			|| ruleKeyList == null || ruleKeyList.isEmpty()) {
			return List.of();
		}
		return springDataNotificationSendRepository
			.findByExperienceIdInAndRuleKeyIn(experienceIdList, ruleKeyList)
			.stream()
			.map(NotificationSendPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	public void saveAll(List<NotificationSend> notificationSendList) {
		if (notificationSendList == null || notificationSendList.isEmpty()) {
			return;
		}
		List<NotificationSendJpaEntity> entities = new ArrayList<>();
		for (NotificationSend send : notificationSendList) {
			entities.add(NotificationSendPersistenceMapper.toNewEntity(send));
		}
		springDataNotificationSendRepository.saveAll(entities);
		springDataNotificationSendRepository.flush();
	}
}
