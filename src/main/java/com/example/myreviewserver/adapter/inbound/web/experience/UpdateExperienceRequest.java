package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.domain.experience.ExperienceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "체험 부분 수정 요청. 보낸 필드만 바꿉니다.")
public record UpdateExperienceRequest(
	@Schema(description = "이름", example = "성수 카페", nullable = true)
	String name,

	@Schema(description = "체험 유형", example = "VISIT", nullable = true)
	ExperienceType experienceType,

	@Schema(description = "예약일", example = "2026-08-20", nullable = true)
	LocalDate reservationDate,

	@Schema(description = "예약 시각", example = "14:00:00", nullable = true)
	LocalTime reservationTime,

	@Schema(description = "리뷰 마감일", example = "2026-08-25", nullable = true)
	LocalDate reviewDeadline,

	@Schema(description = "상세 링크", example = "https://example.com", nullable = true)
	String detailLink,

	@Schema(description = "연결 플랫폼 목록 (보낼 때만 통째로 교체)", nullable = true)
	List<CreateExperiencePlatformRequest> platformList,

	@Schema(
		description = """
			제출 완료 체험 수정 시 필수. 프론트 확인("이미 제출이 완료된 체험입니다. 레뷰 제출을 취소하시겠습니까?")에서
			예이면 true. true면 제출을 해제한 뒤 수정을 반영하고, false/미전달이면 수정을 거부합니다.
			미제출 체험에서는 무시됩니다.
			""",
		example = "true",
		nullable = true
	)
	Boolean cancelSubmission
) {
}
