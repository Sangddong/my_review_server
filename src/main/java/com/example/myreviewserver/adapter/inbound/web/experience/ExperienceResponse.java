package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.domain.experience.Experience;
import com.example.myreviewserver.domain.experience.ExperienceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "체험 목록 항목")
public record ExperienceResponse(
	@Schema(description = "체험 ID", example = "1")
	Long id,

	@Schema(description = "이름", example = "성수 카페")
	String name,

	@Schema(description = "체험 유형", example = "VISIT")
	ExperienceType experienceType,

	@Schema(description = "예약일", example = "2026-08-20", nullable = true)
	LocalDate reservationDate,

	@Schema(description = "예약 시각", example = "14:00:00", nullable = true)
	LocalTime reservationTime,

	@Schema(description = "리뷰 마감일", example = "2026-08-25")
	LocalDate reviewDeadline,

	@Schema(description = "리뷰 제출 여부", example = "false")
	boolean reviewSubmitted,

	@Schema(description = "상세 링크", example = "https://example.com", nullable = true)
	String detailLink,

	@Schema(description = "필수 플랫폼 등록이 모두 끝났는지 (저장값 아님)", example = "false")
	boolean requiredItemsComplete,

	@Schema(description = "연결된 플랫폼")
	List<ExperiencePlatformResponse> platforms
) {

	public static ExperienceResponse from(Experience experience) {
		return new ExperienceResponse(
			experience.getId(),
			experience.getName(),
			experience.getExperienceType(),
			experience.getReservationDate(),
			experience.getReservationTime(),
			experience.getReviewDeadline(),
			experience.isReviewSubmitted(),
			experience.getDetailLink(),
			experience.isRequiredItemsComplete(),
			experience.getPlatforms().stream().map(ExperiencePlatformResponse::from).toList()
		);
	}
}
