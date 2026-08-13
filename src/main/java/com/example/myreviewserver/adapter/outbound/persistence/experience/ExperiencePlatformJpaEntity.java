package com.example.myreviewserver.adapter.outbound.persistence.experience;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA mapping for experience_platforms table.
 *
 * @EmbeddedId: 복합 PK.
 */
@Entity
@Table(name = "experience_platforms")
public class ExperiencePlatformJpaEntity {

	@EmbeddedId
	private ExperiencePlatformId id;

	@Column(name = "is_required")
	private Integer isRequired;

	protected ExperiencePlatformJpaEntity() {
	}

	public ExperiencePlatformJpaEntity(ExperiencePlatformId id, Integer isRequired) {
		this.id = id;
		this.isRequired = isRequired;
	}

	public ExperiencePlatformId getId() {
		return id;
	}

	public Integer getIsRequired() {
		return isRequired;
	}
}
