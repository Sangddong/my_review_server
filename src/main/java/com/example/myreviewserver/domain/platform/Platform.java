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
		if (sortOrder < 0) {
			throw new DomainException("sortOrder must be >= 0");
		}
		return new Platform(null, userId, validatedName(name), validatedColor(color), sortOrder, null, null, null);
	}

	public static String validatedName(String name) {
		validateName(name);
		return name.trim();
	}

	public static String validatedColor(String color) {
		return normalizeColor(color);
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

	public static String requireName(String name) {
		validateName(name);
		return name.trim();
	}

	public static String requireColor(String color) {
		return normalizeColor(color);
	}

	public void rename(String name) {
		ensureActive();
		this.name = validatedName(name);
	}

	public void changeColor(String color) {
		ensureActive();
		this.color = validatedColor(color);
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

	private static String normalizeColor(String color) {
		if (color == null || color.isBlank()) {
			throw new DomainException("color is required");
		}
		String value = color.trim().toLowerCase();
		if (value.matches("#[0-9a-f]{6}")) {
			return value;
		}
		if (value.matches("#[0-9a-f]{3}")) {
			return "#" + value.charAt(1) + value.charAt(1)
				+ value.charAt(2) + value.charAt(2)
				+ value.charAt(3) + value.charAt(3);
		}
		throw new DomainException("color must be a hex code like #c6f8c8");
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
