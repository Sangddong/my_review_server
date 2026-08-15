package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperiencePlatform;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.experience.ExperienceType;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates an experience owned by the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class CreateExperienceUseCase {

	private final ExperienceRepository experienceRepository;
	private final PlatformRepository platformRepository;

	public CreateExperienceUseCase(
		ExperienceRepository experienceRepository,
		PlatformRepository platformRepository
	) {
		this.experienceRepository = experienceRepository;
		this.platformRepository = platformRepository;
	}

	public Experience create(
		Long userId,
		String name,
		ExperienceType experienceType,
		LocalDate reservationDate,
		LocalTime reservationTime,
		LocalDate reviewDeadline,
		String detailLink,
		List<PlatformLink> platformList
	) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (platformList == null || platformList.isEmpty()) {
			throw new DomainException("at least one platform is required");
		}

		List<Long> platformIdList = new ArrayList<>();
		List<ExperiencePlatform> links = new ArrayList<>();
		for (PlatformLink platform : platformList) {
			if (platform == null) {
				throw new DomainException("platform link is required");
			}
			if (platform.platformId() == null) {
				throw new DomainException("platformId is required");
			}
			if (platform.isRequired() == null) {
				throw new DomainException("isRequired is required");
			}
			platformIdList.add(platform.platformId());
			links.add(ExperiencePlatform.of(platform.platformId(), platform.isRequired()));
		}

		Experience experience = Experience.create(
			userId,
			name,
			experienceType,
			reservationDate,
			reservationTime,
			reviewDeadline,
			detailLink,
			links
		);

		long foundCount = platformRepository.countActiveByUserIdAndIdIn(userId, platformIdList);
		if (foundCount != platformIdList.size()) {
			throw new DomainException("Platform not found");
		}

		return experienceRepository.save(experience);
	}

	public record PlatformLink(Long platformId, Boolean isRequired) {
	}
}
