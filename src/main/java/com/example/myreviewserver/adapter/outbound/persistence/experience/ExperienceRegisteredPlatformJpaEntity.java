package com.example.myreviewserver.adapter.outbound.persistence.experience;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mapping for experience_registered_platforms table.
 * Row present = registration complete.
 *
 * @EmbeddedId: 복합 PK.
 */
@Entity
@Table(name = "experience_registered_platforms")
public class ExperienceRegisteredPlatformJpaEntity {

	@EmbeddedId
	private ExperiencePlatformId id;

	@Column(name = "registered_at", nullable = false)
	private Instant registeredAt;

	protected ExperienceRegisteredPlatformJpaEntity() {
	}

	public ExperienceRegisteredPlatformJpaEntity(ExperiencePlatformId id, Instant registeredAt) {
		this.id = id;
		this.registeredAt = registeredAt;
	}

	public ExperiencePlatformId getId() {
		return id;
	}

	public Instant getRegisteredAt() {
		return registeredAt;
	}
}
