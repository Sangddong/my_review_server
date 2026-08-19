package com.example.myreviewserver.adapter.outbound.persistence.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationSendRepository extends JpaRepository<NotificationSendJpaEntity, Long> {

	List<NotificationSendJpaEntity> findByExperienceIdInAndRuleKeyIn(
		List<Long> experienceIds,
		List<String> ruleKeys
	);
}
