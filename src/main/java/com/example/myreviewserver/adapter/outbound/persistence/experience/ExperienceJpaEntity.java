package com.example.myreviewserver.adapter.outbound.persistence.experience;

import com.example.myreviewserver.domain.experience.ExperienceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA mapping for experiences table.
 *
 * @Entity: JPA가 관리하는 테이블 행.
 * @Table: DB 테이블 이름.
 * @Enumerated: enum을 문자열(VISIT 등)로 저장.
 */
@Entity
@Table(name = "experiences")
public class ExperienceJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "experience_type", nullable = false, length = 20)
	private ExperienceType experienceType;

	@Column(name = "reservation_date")
	private LocalDate reservationDate;

	@Column(name = "reservation_time")
	private LocalTime reservationTime;

	@Column(name = "review_deadline", nullable = false)
	private LocalDate reviewDeadline;

	@Column(name = "is_review_submitted")
	private Integer isReviewSubmitted;

	@Column(name = "detail_link", length = 1000)
	private String detailLink;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Instant updatedAt;

	protected ExperienceJpaEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExperienceType getExperienceType() {
		return experienceType;
	}

	public void setExperienceType(ExperienceType experienceType) {
		this.experienceType = experienceType;
	}

	public LocalDate getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(LocalDate reservationDate) {
		this.reservationDate = reservationDate;
	}

	public LocalTime getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(LocalTime reservationTime) {
		this.reservationTime = reservationTime;
	}

	public LocalDate getReviewDeadline() {
		return reviewDeadline;
	}

	public void setReviewDeadline(LocalDate reviewDeadline) {
		this.reviewDeadline = reviewDeadline;
	}

	public Integer getIsReviewSubmitted() {
		return isReviewSubmitted;
	}

	public void setIsReviewSubmitted(Integer isReviewSubmitted) {
		this.isReviewSubmitted = isReviewSubmitted;
	}

	public String getDetailLink() {
		return detailLink;
	}

	public void setDetailLink(String detailLink) {
		this.detailLink = detailLink;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
