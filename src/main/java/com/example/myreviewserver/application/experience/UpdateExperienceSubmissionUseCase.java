package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sets review submission for an experience owned by the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdateExperienceSubmissionUseCase {

	private final ExperienceRepository experienceRepository;

	public UpdateExperienceSubmissionUseCase(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	public Experience update(Long userId, Long experienceId, Boolean submitted) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}
		if (submitted == null) {
			throw new DomainException("submitted is required");
		}

		Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
			.orElseThrow(() -> new DomainException("Experience not found"));
		if (submitted) {
			experience.submitReview();
		}
		else {
			experience.unsubmitReview();
		}
		return experienceRepository.save(experience);
	}
}
