package com.example.myreviewserver.domain.platform;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

/**
 * Platform aggregate (user-owned review channel).
 * Soft delete uses is_deleted: null = active, 1 = deleted.
 */
public class Platform {

	private final Long id;
	private final Long userId;
	private String name;
	private String color;
	private int sortOrder;
	private Integer isDeleted;
	private final Instant createdAt;
	private final Instant updatedAt;

	private Platform(
		Long id,
		Long userId,
		String name,
		String color,
		int sortOrder,
		Integer isDeleted,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.color = color;
		this.sortOrder = sortOrder;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Platform create(Long userId, String name, String color, int sortOrder) {
		validateUserId(userId);
		validateName(name);
		validateColor(color);
		if (sortOrder < 0) {
			throw new DomainException("sortOrder must be >= 0");
		}
		return new Platform(null, userId, name.trim(), color.trim(), sortOrder, null, null, null);
	}

	public static Platform restore(
		Long id,
		Long userId,
		String name,
		String color,
		int sortOrder,
		Integer isDeleted,
		Instant createdAt,
		Instant updatedAt
	) {
		return new Platform(id, userId, name, color, sortOrder, isDeleted, createdAt, updatedAt);
	}

	public void rename(String name) {
		ensureActive();
		validateName(name);
		this.name = name.trim();
	}

	public void changeColor(String color) {
		ensureActive();
		validateColor(color);
		this.color = color.trim();
	}

	public void changeSortOrder(int sortOrder) {
		ensureActive();
		if (sortOrder < 0) {
			throw new DomainException("sortOrder must be >= 0");
		}
		this.sortOrder = sortOrder;
	}

	public void softDelete() {
		ensureActive();
		this.isDeleted = 1;
	}

	public boolean isActive() {
		return isDeleted == null;
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

	public String getColor() {
		return color;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	private void ensureActive() {
		if (!isActive()) {
			throw new DomainException("Platform is deleted");
		}
	}

	private static void validateUserId(Long userId) {
		if (userId == null || userId <= 0) {
			throw new DomainException("userId is required");
		}
	}

	private static void validateName(String name) {
		if (name == null || name.isBlank()) {
			throw new DomainException("name is required");
		}
		if (name.trim().length() > 100) {
			throw new DomainException("name must be <= 100 characters");
		}
	}

	private static void validateColor(String color) {
		if (color == null || color.isBlank()) {
			throw new DomainException("color is required");
		}
		if (color.trim().length() > 100) {
			throw new DomainException("color must be <= 100 characters");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Platform platform)) {
			return false;
		}
		return id != null && Objects.equals(id, platform.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
