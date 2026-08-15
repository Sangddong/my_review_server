package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns one experience owned by the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션 (readOnly = 조회만).
 */
@Service
@Transactional(readOnly = true)
public class GetExperienceUseCase {

	private final ExperienceRepository experienceRepository;

	public GetExperienceUseCase(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	public Experience get(Long userId, Long experienceId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}
		return experienceRepository.findByIdAndUserId(experienceId, userId)
			.orElseThrow(() -> new DomainException("Experience not found"));
	}
}
