package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the authenticated user's upcoming or completed experiences.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션 (readOnly = 조회만).
 */
@Service
@Transactional(readOnly = true)
public class ListExperiencesUseCase {

	private final ExperienceRepository experienceRepository;

	public ListExperiencesUseCase(ExperienceRepository experienceRepository) {
		this.experienceRepository = experienceRepository;
	}

	public List<Experience> upcoming(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		return experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(userId);
	}

	public List<Experience> completed(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		return experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(userId);
	}
}
