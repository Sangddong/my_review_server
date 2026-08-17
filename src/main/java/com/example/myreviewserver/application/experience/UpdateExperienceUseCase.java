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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Partially updates an experience owned by the authenticated user.
 * Submission and per-platform registration are not changed here.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdateExperienceUseCase {

	private final ExperienceRepository experienceRepository;
	private final PlatformRepository platformRepository;

	public UpdateExperienceUseCase(
		ExperienceRepository experienceRepository,
		PlatformRepository platformRepository
	) {
		this.experienceRepository = experienceRepository;
		this.platformRepository = platformRepository;
	}

	public Experience update(
		Long userId,
		Long experienceId,
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
		if (experienceId == null) {
			throw new DomainException("experienceId is required");
		}

		boolean hasReservation = reservationDate != null || reservationTime != null;
		if (name == null
			&& experienceType == null
			&& !hasReservation
			&& reviewDeadline == null
			&& detailLink == null
			&& platformList == null) {
			throw new DomainException("at least one field is required");
		}

		Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
			.orElseThrow(() -> new DomainException("Experience not found"));

		if (name != null) {
			experience.rename(name);
		}
		if (experienceType != null) {
			experience.changeType(experienceType);
		}
		if (hasReservation) {
			experience.changeReservation(
				reservationDate != null ? reservationDate : experience.getReservationDate(),
				reservationTime != null ? reservationTime : experience.getReservationTime()
			);
		}
		if (reviewDeadline != null) {
			experience.changeReviewDeadline(reviewDeadline);
		}
		if (detailLink != null) {
			experience.changeDetailLink(detailLink);
		}
		if (platformList != null) {
			experience.replacePlatformList(toLinksPreservingRegistered(userId, experience, platformList));
		}

		return experienceRepository.save(experience);
	}

	private List<ExperiencePlatform> toLinksPreservingRegistered(
		Long userId,
		Experience experience,
		List<PlatformLink> platformList
	) {
		if (platformList.isEmpty()) {
			throw new DomainException("at least one platform is required");
		}

		Map<Long, Boolean> registeredByPlatformId = experience.getPlatformList().stream()
			.collect(Collectors.toMap(ExperiencePlatform::getPlatformId, ExperiencePlatform::isRegistered));

		Set<Long> platformIdList = new LinkedHashSet<>();
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
			links.add(ExperiencePlatform.of(
				platform.platformId(),
				platform.isRequired(),
				registeredByPlatformId.getOrDefault(platform.platformId(), false)
			));
		}

		long foundCount = platformRepository.countActiveByUserIdAndIdIn(userId, List.copyOf(platformIdList));
		if (foundCount != platformIdList.size()) {
			throw new DomainException("Platform not found");
		}
		return links;
	}

	public record PlatformLink(Long platformId, Boolean isRequired) {
	}
}
