package com.example.myreviewserver.adapter.inbound.web.me;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "알림 삭제 요청")
public record NotificationDeleteRequest(
	@Schema(description = "삭제할 알림 id 목록", example = "[1, 2, 3]")
	List<Long> idList
) {
}
