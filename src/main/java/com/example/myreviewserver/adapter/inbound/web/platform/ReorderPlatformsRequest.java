package com.example.myreviewserver.adapter.inbound.web.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "플랫폼 정렬 요청")
public record ReorderPlatformsRequest(
	@Schema(description = "활성 플랫폼 id를 원하는 표시 순서로 나열", example = "[3, 1, 2]")
	List<Long> orderedIds
) {
}
