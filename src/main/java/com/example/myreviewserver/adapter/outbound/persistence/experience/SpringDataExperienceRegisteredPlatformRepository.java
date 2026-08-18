package com.example.myreviewserver.adapter.outbound.persistence.experience;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExperienceRegisteredPlatformRepository
	extends JpaRepository<ExperienceRegisteredPlatformJpaEntity, ExperiencePlatformId> {

	List<ExperienceRegisteredPlatformJpaEntity> findByIdExperienceId(Long experienceId);

	void deleteByIdExperienceId(Long experienceId);

	void deleteByIdExperienceIdIn(List<Long> experienceIds);
}
