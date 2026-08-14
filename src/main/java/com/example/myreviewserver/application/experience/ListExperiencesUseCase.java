package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the authenticated user's experiences filtered by upcoming or completed.
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

	public List<Experience> execute(Long userId, ExperienceListStatus status) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (status == null) {
			throw new DomainException("status is required");
		}
		if (status == ExperienceListStatus.completed) {
			return experienceRepository.findCompletedByUserIdOrderByReservationAscIdAsc(userId);
		}
		return experienceRepository.findUpcomingByUserIdOrderByReservationAscIdAsc(userId);
	}
}
