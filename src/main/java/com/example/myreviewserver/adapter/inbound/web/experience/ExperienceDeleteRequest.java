package com.example.myreviewserver.adapter.inbound.web.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "체험 삭제 요청")
public record ExperienceDeleteRequest(
	@Schema(description = "삭제할 체험 id 목록", example = "[1, 2, 3]")
	List<Long> idList
) {
}
