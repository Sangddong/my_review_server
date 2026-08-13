package com.example.myreviewserver.adapter.outbound.persistence.experience;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExperiencePlatformRepository
	extends JpaRepository<ExperiencePlatformJpaEntity, ExperiencePlatformId> {

	List<ExperiencePlatformJpaEntity> findByIdExperienceId(Long experienceId);

	void deleteByIdExperienceId(Long experienceId);
}
