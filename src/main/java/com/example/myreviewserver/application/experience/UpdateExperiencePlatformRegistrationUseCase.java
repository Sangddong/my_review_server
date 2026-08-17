package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sets registration complete/incomplete for one platform linked to an experience.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdateExperiencePlatformRegistrationUseCase {

	private final ExperienceRepository experienceRepository;

	public UpdateExperiencePlatformRegistrationUseCase(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	public Experience update(Long userId, Long experienceId, Long platformId, Boolean registered) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}
		if (platformId == null) {
			throw new DomainException("platformId is required");
		}
		if (registered == null) {
			throw new DomainException("registered is required");
		}

		Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
			.orElseThrow(() -> new DomainException("Experience not found"));
		experience.setPlatformRegistered(platformId, registered);
		return experienceRepository.save(experience);
	}
}
