package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.domain.experience.ExperienceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "체험 생성 요청")
public record CreateExperienceRequest(
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

	@Schema(description = "상세 링크", example = "https://example.com", nullable = true)
	String detailLink,

	@Schema(description = "연결 플랫폼 목록 (필수 플랫폼 1개 이상)")
	List<CreateExperiencePlatformRequest> platformList
) {
}
