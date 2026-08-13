package com.example.myreviewserver.domain.experience;

import com.example.myreviewserver.domain.shared.DomainException;
import java.util.Objects;

/**
 * Platform link on an Experience: required flag + registration state.
 * is_required: null = optional, 1 = required.
 * registered: row exists in experience_registered_platforms.
 */
public final class ExperiencePlatform {

	private final Long platformId;
	private final boolean required;
	private final boolean registered;

	private ExperiencePlatform(Long platformId, boolean required, boolean registered) {
		if (platformId == null || platformId <= 0) {
			throw new DomainException("platformId is required");
		}
		this.platformId = platformId;
		this.required = required;
		this.registered = registered;
	}

	public static ExperiencePlatform of(Long platformId, boolean required) {
		return new ExperiencePlatform(platformId, required, false);
	}

	public static ExperiencePlatform of(Long platformId, boolean required, boolean registered) {
		return new ExperiencePlatform(platformId, required, registered);
	}

	public ExperiencePlatform withRegistered(boolean registered) {
		return new ExperiencePlatform(platformId, required, registered);
	}

	public Long getPlatformId() {
		return platformId;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isRegistered() {
		return registered;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExperiencePlatform that)) {
			return false;
		}
		return Objects.equals(platformId, that.platformId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(platformId);
	}
}
