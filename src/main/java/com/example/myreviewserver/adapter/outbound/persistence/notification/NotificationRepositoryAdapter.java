package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.Notification;
import com.example.myreviewserver.domain.notification.NotificationRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter for notifications (user inbox).
 *
 * @Repository: 영속성 컴포넌트.
 * @Transactional: DB 트랜잭션.
 */
@Repository
@Transactional
public class NotificationRepositoryAdapter implements NotificationRepository {

	private final SpringDataNotificationRepository springDataNotificationRepository;
	private final EntityManager entityManager;

	public NotificationRepositoryAdapter(
		SpringDataNotificationRepository springDataNotificationRepository,
		EntityManager entityManager
	) {
		this.springDataNotificationRepository = springDataNotificationRepository;
		this.entityManager = entityManager;
	}

	@Override
	public Notification save(Notification notification) {
		NotificationJpaEntity entity;
		if (notification.getId() == null) {
			entity = NotificationPersistenceMapper.toNewEntity(notification);
		}
		else {
			entity = springDataNotificationRepository
				.findActiveByIdAndUserId(notification.getId(), notification.getUserId())
				.orElseThrow(() -> new DomainException("Notification not found"));
			NotificationPersistenceMapper.copyToEntity(notification, entity);
		}
		NotificationJpaEntity saved = springDataNotificationRepository.saveAndFlush(entity);
		entityManager.refresh(saved);
		return NotificationPersistenceMapper.toDomain(saved);
	}

	@Override
	public void saveAll(List<Notification> notificationList) {
		if (notificationList == null || notificationList.isEmpty()) {
			return;
		}
		List<NotificationJpaEntity> entities = new ArrayList<>();
		for (Notification notification : notificationList) {
			entities.add(NotificationPersistenceMapper.toNewEntity(notification));
		}
		springDataNotificationRepository.saveAll(entities);
		springDataNotificationRepository.flush();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Notification> findByIdAndUserId(Long id, Long userId) {
		return springDataNotificationRepository.findActiveByIdAndUserId(id, userId)
			.map(NotificationPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Long userId) {
		return springDataNotificationRepository.findActiveByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
			.map(NotificationPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	public int markAllUnreadAsReadByUserId(Long userId) {
		return springDataNotificationRepository.markAllUnreadAsReadByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public long countUnreadByUserId(Long userId) {
		return springDataNotificationRepository.countUnreadByUserId(userId);
	}

	@Override
	public int softDeleteByUserIdAndIdIn(Long userId, List<Long> idList) {
		return springDataNotificationRepository.softDeleteByUserIdAndIdIn(userId, idList, Instant.now());
	}

	@Override
	public int deleteAllDeletedBefore(Instant cutoff) {
		return springDataNotificationRepository.deleteAllDeletedBefore(cutoff);
	}
}
