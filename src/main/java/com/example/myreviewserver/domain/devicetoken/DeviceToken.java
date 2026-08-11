package com.example.myreviewserver.domain.devicetoken;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import java.util.Objects;

/**
 * Push device token owned by a user (FCM etc.).
 */
public class DeviceToken {

	private static final int TOKEN_MAX_LENGTH = 512;

	private final Long id;
	private Long userId;
	private String token;
	private DevicePlatform platform;
	private final Instant createdAt;
	private final Instant updatedAt;

	private DeviceToken(
		Long id,
		Long userId,
		String token,
		DevicePlatform platform,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.token = token;
		this.platform = platform;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static DeviceToken create(Long userId, String token, DevicePlatform platform) {
		validateUserId(userId);
		validateToken(token);
		validatePlatform(platform);
		return new DeviceToken(null, userId, token.trim(), platform, null, null);
	}

	public static DeviceToken restore(
		Long id,
		Long userId,
		String token,
		DevicePlatform platform,
		Instant createdAt,
		Instant updatedAt
	) {
		return new DeviceToken(id, userId, token, platform, createdAt, updatedAt);
	}

	public void reassignTo(Long userId, DevicePlatform platform) {
		validateUserId(userId);
		validatePlatform(platform);
		this.userId = userId;
		this.platform = platform;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getToken() {
		return token;
	}

	public DevicePlatform getPlatform() {
		return platform;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	private static void validateUserId(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
	}

	private static void validateToken(String token) {
		if (token == null || token.isBlank()) {
			throw new DomainException("token is required");
		}
		if (token.trim().length() > TOKEN_MAX_LENGTH) {
			throw new DomainException("token must be <= " + TOKEN_MAX_LENGTH + " characters");
		}
	}

	private static void validatePlatform(DevicePlatform platform) {
		if (platform == null) {
			throw new DomainException("platform is required");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DeviceToken that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
