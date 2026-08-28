package com.example.myreviewserver.adapter.outbound.persistence.notification;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationSettingRepository
	extends JpaRepository<NotificationSettingJpaEntity, Long> {

	Optional<NotificationSettingJpaEntity> findByUserIdAndRuleKey(Long userId, NotificationRuleKey ruleKey);

	List<NotificationSettingJpaEntity> findByUserIdOrderByIdAsc(Long userId);

	List<NotificationSettingJpaEntity> findByUserIdInAndEnabledFalse(List<Long> userIds);
}
