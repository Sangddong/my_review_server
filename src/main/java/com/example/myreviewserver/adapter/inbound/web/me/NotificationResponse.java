package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.domain.notification.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "알림")
public record NotificationResponse(
	@Schema(description = "알림 ID", example = "1")
	Long id,

	@Schema(description = "관련 체험 ID", example = "10")
	Long experienceId,

	@Schema(description = "알림 규칙 키", example = "D3")
	String ruleKey,

	@Schema(description = "제목", example = "넥쿨러 리뷰 마감 3일 전입니다")
	String title,

	@Schema(description = "본문", example = "마감일 전에 리뷰를 작성하여 제출해주세요")
	String body,

	@Schema(description = "읽음 여부", example = "false")
	boolean isRead,

	@Schema(description = "생성 시각")
	Instant createdAt
) {

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
			notification.getId(),
			notification.getExperienceId(),
			notification.getRuleKey(),
			notification.getTitle(),
			notification.getBody(),
			notification.isRead(),
			notification.getCreatedAt()
		);
	}
}
