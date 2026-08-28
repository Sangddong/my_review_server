package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import com.example.myreviewserver.domain.notification.NotificationSetting;
import com.example.myreviewserver.domain.notification.NotificationSettingRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter for notification_settings.
 *
 * @Repository: 영속성 컴포넌트.
 * @Transactional: DB 트랜잭션.
 */
@Repository
@Transactional
public class NotificationSettingRepositoryAdapter implements NotificationSettingRepository {

	private final SpringDataNotificationSettingRepository springDataNotificationSettingRepository;
	private final EntityManager entityManager;

	public NotificationSettingRepositoryAdapter(
		SpringDataNotificationSettingRepository springDataNotificationSettingRepository,
		EntityManager entityManager
	) {
		this.springDataNotificationSettingRepository = springDataNotificationSettingRepository;
		this.entityManager = entityManager;
	}

	@Override
	public NotificationSetting save(NotificationSetting notificationSetting) {
		NotificationSettingJpaEntity entity;
		if (notificationSetting.getId() == null) {
			entity = NotificationSettingPersistenceMapper.toNewEntity(notificationSetting);
		}
		else {
			entity = springDataNotificationSettingRepository.findById(notificationSetting.getId())
				.orElseThrow(() -> new DomainException("Notification setting not found"));
			NotificationSettingPersistenceMapper.copyToEntity(notificationSetting, entity);
		}
		NotificationSettingJpaEntity saved = springDataNotificationSettingRepository.saveAndFlush(entity);
		entityManager.refresh(saved);
		return NotificationSettingPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<NotificationSetting> findByUserIdAndRuleKey(Long userId, NotificationRuleKey ruleKey) {
		return springDataNotificationSettingRepository.findByUserIdAndRuleKey(userId, ruleKey)
			.map(NotificationSettingPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationSetting> findAllByUserId(Long userId) {
		return springDataNotificationSettingRepository.findByUserIdOrderByIdAsc(userId).stream()
			.map(NotificationSettingPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationSetting> findDisabledByUserIdIn(List<Long> userIdList) {
		if (userIdList == null || userIdList.isEmpty()) {
			return List.of();
		}
		return springDataNotificationSettingRepository.findByUserIdInAndEnabledFalse(userIdList).stream()
			.map(NotificationSettingPersistenceMapper::toDomain)
			.toList();
	}
}
