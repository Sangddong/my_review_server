package com.example.myreviewserver.adapter.inbound.web.experience;

import com.example.myreviewserver.application.experience.ExtractedExperienceDraft;
import com.example.myreviewserver.domain.experience.ExperienceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "스크린샷에서 읽은 체험 입력값. 예약일·예약 시각은 포함하지 않습니다.")
public record ExtractedExperienceResponse(
	@Schema(description = "이름. 확신할 수 없으면 null", example = "버질스퀘어 성수", nullable = true)
	String name,

	@Schema(description = "체험 유형. 확신할 수 없으면 null", example = "VISIT", nullable = true)
	ExperienceType experienceType,

	@Schema(description = "리뷰 마감일. 신청 마감은 넣지 않으며, 확신할 수 없으면 null", example = "2026-11-13", nullable = true)
	LocalDate reviewDeadline,

	@Schema(description = "모델이 판단한 플랫폼 이름. 저장된 태그와 맞으면 그 태그 이름입니다.")
	List<String> platformNames,

	@Schema(description = "platformNames 중 이 사용자의 플랫폼으로 맞춰진 id. 화면은 이 값으로 태그를 고릅니다.")
	List<Long> platformIds,

	@Schema(description = "상세 링크. 화면에 없으면 null", example = "https://example.com/campaigns/1", nullable = true)
	String detailLink
) {

	public static ExtractedExperienceResponse from(ExtractedExperienceDraft draft) {
		return new ExtractedExperienceResponse(
			draft.name(),
			draft.experienceType(),
			draft.reviewDeadline(),
			draft.platformNames(),
			draft.platformIds(),
			draft.detailLink()
		);
	}
}
