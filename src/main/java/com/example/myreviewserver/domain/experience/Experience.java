package com.example.myreviewserver.domain.experience;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Experience aggregate (user-owned review campaign).
 * requiredItemsComplete is derived from required + registered platform links.
 * is_review_submitted: null = not submitted, 1 = submitted.
 */
public class Experience {

	private static final int NAME_MAX_LENGTH = 200;
	private static final int DETAIL_LINK_MAX_LENGTH = 1000;

	private final Long id;
	private final Long userId;
	private String name;
	private ExperienceType experienceType;
	private LocalDate reservationDate;
	private LocalTime reservationTime;
	private LocalDate reviewDeadline;
	private Integer isReviewSubmitted;
	private String detailLink;
	private List<ExperiencePlatform> platforms;
	private final Instant createdAt;
	private final Instant updatedAt;

	private Experience(
		Long id,
		Long userId,
		String name,
		ExperienceType experienceType,
		LocalDate reservationDate,
		LocalTime reservationTime,
		LocalDate reviewDeadline,
		Integer isReviewSubmitted,
		String detailLink,
		List<ExperiencePlatform> platforms,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.experienceType = experienceType;
		this.reservationDate = reservationDate;
		this.reservationTime = reservationTime;
		this.reviewDeadline = reviewDeadline;
		this.isReviewSubmitted = isReviewSubmitted;
		this.detailLink = detailLink;
		this.platforms = platforms;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Experience create(
		Long userId,
		String name,
		ExperienceType experienceType,
		LocalDate reservationDate,
		LocalTime reservationTime,
		LocalDate reviewDeadline,
		String detailLink,
		List<ExperiencePlatform> platforms
	) {
		validateUserId(userId);
		return new Experience(
			null,
			userId,
			validatedName(name),
			validatedType(experienceType),
			reservationDate,
			reservationTime,
			validatedDeadline(reviewDeadline),
			null,
			validatedDetailLink(detailLink),
			validatedPlatforms(platforms),
			null,
			null
		);
	}

	public static Experience restore(
		Long id,
		Long userId,
		String name,
		ExperienceType experienceType,
		LocalDate reservationDate,
		LocalTime reservationTime,
		LocalDate reviewDeadline,
		Integer isReviewSubmitted,
		String detailLink,
		List<ExperiencePlatform> platforms,
		Instant createdAt,
		Instant updatedAt
	) {
		return new Experience(
			id,
			userId,
			name,
			experienceType,
			reservationDate,
			reservationTime,
			reviewDeadline,
			isReviewSubmitted,
			detailLink,
			new ArrayList<>(platforms),
			createdAt,
			updatedAt
		);
	}

	public void rename(String name) {
		this.name = validatedName(name);
	}

	public void changeType(ExperienceType experienceType) {
		this.experienceType = validatedType(experienceType);
	}

	public void changeReservation(LocalDate reservationDate, LocalTime reservationTime) {
		this.reservationDate = reservationDate;
		this.reservationTime = reservationTime;
	}

	public void changeReviewDeadline(LocalDate reviewDeadline) {
		this.reviewDeadline = validatedDeadline(reviewDeadline);
	}

	public void changeDetailLink(String detailLink) {
		this.detailLink = validatedDetailLink(detailLink);
	}

	public void replacePlatforms(List<ExperiencePlatform> platforms) {
		this.platforms = validatedPlatforms(platforms);
	}

	public void setPlatformRegistered(Long platformId, boolean registered) {
		boolean found = false;
		List<ExperiencePlatform> next = new ArrayList<>();
		for (ExperiencePlatform platform : platforms) {
			if (platform.getPlatformId().equals(platformId)) {
				next.add(platform.withRegistered(registered));
				found = true;
			}
			else {
				next.add(platform);
			}
		}
		if (!found) {
			throw new DomainException("Platform is not linked to this experience");
		}
		this.platforms = next;
	}

	public void submitReview() {
		this.isReviewSubmitted = 1;
	}

	public void unsubmitReview() {
		this.isReviewSubmitted = null;
	}

	public boolean isReviewSubmitted() {
		return isReviewSubmitted != null;
	}

	/**
	 * Derived: every required platform has registration completed.
	 */
	public boolean isRequiredItemsComplete() {
		return platforms.stream()
			.filter(ExperiencePlatform::isRequired)
			.allMatch(ExperiencePlatform::isRegistered);
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public ExperienceType getExperienceType() {
		return experienceType;
	}

	public LocalDate getReservationDate() {
		return reservationDate;
	}

	public LocalTime getReservationTime() {
		return reservationTime;
	}

	public LocalDate getReviewDeadline() {
		return reviewDeadline;
	}

	public Integer getIsReviewSubmitted() {
		return isReviewSubmitted;
	}

	public String getDetailLink() {
		return detailLink;
	}

	public List<ExperiencePlatform> getPlatforms() {
		return List.copyOf(platforms);
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	private static void validateUserId(Long userId) {
		if (userId == null || userId <= 0) {
			throw new DomainException("userId is required");
		}
	}

	private static String validatedName(String name) {
		if (name == null || name.isBlank()) {
			throw new DomainException("name is required");
		}
		String trimmed = name.trim();
		if (trimmed.length() > NAME_MAX_LENGTH) {
			throw new DomainException("name must be <= " + NAME_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	private static ExperienceType validatedType(ExperienceType experienceType) {
		if (experienceType == null) {
			throw new DomainException("experienceType is required");
		}
		return experienceType;
	}

	private static LocalDate validatedDeadline(LocalDate reviewDeadline) {
		if (reviewDeadline == null) {
			throw new DomainException("reviewDeadline is required");
		}
		return reviewDeadline;
	}

	private static String validatedDetailLink(String detailLink) {
		if (detailLink == null || detailLink.isBlank()) {
			return null;
		}
		String trimmed = detailLink.trim();
		if (trimmed.length() > DETAIL_LINK_MAX_LENGTH) {
			throw new DomainException("detailLink must be <= " + DETAIL_LINK_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	private static List<ExperiencePlatform> validatedPlatforms(List<ExperiencePlatform> platforms) {
		if (platforms == null || platforms.isEmpty()) {
			throw new DomainException("at least one platform is required");
		}
		Set<Long> seen = new HashSet<>();
		List<ExperiencePlatform> copy = new ArrayList<>();
		for (ExperiencePlatform platform : platforms) {
			if (platform == null) {
				throw new DomainException("platform link is required");
			}
			if (!seen.add(platform.getPlatformId())) {
				throw new DomainException("duplicate platformId");
			}
			copy.add(platform);
		}
		return copy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Experience experience)) {
			return false;
		}
		return id != null && Objects.equals(id, experience.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
