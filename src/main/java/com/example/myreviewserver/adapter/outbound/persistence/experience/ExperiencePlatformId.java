package com.example.myreviewserver.adapter.outbound.persistence.experience;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for experience_platforms / experience_registered_platforms.
 *
 * @Embeddable: 여러 컬럼을 묶어 PK로 쓰는 값 타입.
 */
@Embeddable
public class ExperiencePlatformId implements Serializable {

	@Column(name = "experience_id")
	private Long experienceId;

	@Column(name = "platform_id")
	private Long platformId;

	protected ExperiencePlatformId() {
	}

	public ExperiencePlatformId(Long experienceId, Long platformId) {
		this.experienceId = experienceId;
		this.platformId = platformId;
	}

	public Long getExperienceId() {
		return experienceId;
	}

	public Long getPlatformId() {
		return platformId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExperiencePlatformId that)) {
			return false;
		}
		return Objects.equals(experienceId, that.experienceId)
			&& Objects.equals(platformId, that.platformId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(experienceId, platformId);
	}
}
