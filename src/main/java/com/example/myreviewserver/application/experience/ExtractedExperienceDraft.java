package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.experience.ExperienceType;
import java.time.LocalDate;
import java.util.List;

/**
 * Form fields read from a screenshot.
 * Reservation date and time are intentionally absent.
 */
public record ExtractedExperienceDraft(
	String name,
	ExperienceType experienceType,
	LocalDate reviewDeadline,
	List<String> platformNames,
	String detailLink,
	List<Long> platformIds
) {

	public ExtractedExperienceDraft {
		platformNames = platformNames == null ? List.of() : List.copyOf(platformNames);
		platformIds = platformIds == null ? List.of() : List.copyOf(platformIds);
	}

	public ExtractedExperienceDraft(
		String name,
		ExperienceType experienceType,
		LocalDate reviewDeadline,
		List<String> platformNames,
		String detailLink
	) {
		this(name, experienceType, reviewDeadline, platformNames, detailLink, List.of());
	}
}
