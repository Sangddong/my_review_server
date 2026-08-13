package com.example.myreviewserver.adapter.outbound.persistence.experience;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExperienceRepository extends JpaRepository<ExperienceJpaEntity, Long> {

	Optional<ExperienceJpaEntity> findByIdAndUserId(Long id, Long userId);

	List<ExperienceJpaEntity> findByUserIdAndIsReviewSubmittedIsNullOrderByReviewDeadlineAscIdAsc(Long userId);

	List<ExperienceJpaEntity> findByUserIdAndIsReviewSubmittedOrderByReviewDeadlineAscIdAsc(
		Long userId,
		Integer isReviewSubmitted
	);
}
