package com.example.myreviewserver.application.experience;

import com.example.myreviewserver.domain.shared.DomainException;

/**
 * List filter for GET /api/experiences?status=
 */
public enum ExperienceListStatus {
	upcoming,
	completed;

	public static ExperienceListStatus from(String value) {
		if (value == null || value.isBlank()) {
			throw new DomainException("status is required");
		}
		try {
			return ExperienceListStatus.valueOf(value.trim());
		}
		catch (IllegalArgumentException ex) {
			throw new DomainException("status must be upcoming or completed");
		}
	}
}
