package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes experiences owned by the authenticated user.
 * Linked experience_platforms and registered rows are removed with them.
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

	public void delete(Long userId, List<Long> idList) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (idList == null || idList.isEmpty()) {
			throw new DomainException("idList is required");
		}
		if (idList.stream().anyMatch(id -> id == null)) {
			throw new DomainException("idList must not contain null");
		}

		int deleted = experienceRepository.deleteByUserIdAndIdIn(userId, idList);
		if (deleted == 0) {
			throw new DomainException("Experience not found");
		}
	}
}
