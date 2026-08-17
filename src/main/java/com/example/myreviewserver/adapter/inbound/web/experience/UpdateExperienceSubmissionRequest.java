package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체험 제출 상태 변경 요청")
public record UpdateExperienceSubmissionRequest(
	@Schema(description = "리뷰 제출 여부", example = "true")
	Boolean submitted
) {
}
