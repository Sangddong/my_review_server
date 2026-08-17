package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes an experience owned by the authenticated user.
 * Linked experience_platforms and registered rows are removed with it.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class DeleteExperienceUseCase {

	private final ExperienceRepository experienceRepository;

	public DeleteExperienceUseCase(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	public void delete(Long userId, Long experienceId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}

		boolean deleted = experienceRepository.deleteByIdAndUserId(experienceId, userId);
		if (!deleted) {
			throw new DomainException("Experience not found");
		}
	}
}
