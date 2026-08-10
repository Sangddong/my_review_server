package com.example.myreviewserver.domain.user;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

public class User {

	private final Long id;
	private String email;
	private String nickname;
	private Integer isDeleted;
	private Instant deletedAt;
	private Instant lastLoginAt;
	private final Instant createdAt;

	private User(
		Long id,
		String email,
		String nickname,
		Integer isDeleted,
		Instant deletedAt,
		Instant lastLoginAt,
		Instant createdAt
	) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.isDeleted = isDeleted;
		this.deletedAt = deletedAt;
		this.lastLoginAt = lastLoginAt;
		this.createdAt = createdAt;
	}

	public static User create(String email, String nickname) {
		validateNickname(nickname);
		return new User(null, normalizeEmail(email), nickname.trim(), null, null, null, null);
	}

	public static User restore(
		Long id,
		String email,
		String nickname,
		Integer isDeleted,
		Instant deletedAt,
		Instant lastLoginAt,
		Instant createdAt
	) {
		return new User(id, email, nickname, isDeleted, deletedAt, lastLoginAt, createdAt);
	}

	public void markLogin() {
		ensureActive();
		this.lastLoginAt = Instant.now();
	}

	public void ensureActive() {
		if (isDeleted != null) {
			throw new DomainException("User is deleted");
		}
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getNickname() {
		return nickname;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static void validateNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			throw new DomainException("nickname is required");
		}
		if (nickname.trim().length() > 100) {
			throw new DomainException("nickname must be <= 100 characters");
		}
	}

	private static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User user)) {
			return false;
		}
		return id != null && Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
