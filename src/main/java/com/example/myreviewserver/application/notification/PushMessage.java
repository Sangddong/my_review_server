package com.example.myreviewserver.application.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Device push payload. Maps to FCM {@code notification} + {@code data}.
 * Tapping the push should open experience detail using {@code data.screen}
 * and {@code data.experienceId}.
 *
 * @Schema: Swagger에 이 타입 설명을 붙임.
 */
@Schema(
	name = "PushMessage",
	description = """
		푸시 계약 (FCM).
		notification: title, body.
		data: ruleKey, experienceId(문자열), screen.
		앱이 푸시를 탭하면 screen=experience_detail 이고 experienceId가 있으면 해당 체험 상세로 이동한다.
		"""
)
public record PushMessage(
	@Schema(description = "알림 제목 (notification.title)", example = "넥쿨러 리뷰 마감 3일 전입니다")
	String title,

	@Schema(description = "알림 본문 (notification.body)", example = "마감일 전에 리뷰를 작성하여 제출해주세요")
	String body,

	@Schema(description = "알림 규칙 키 (data.ruleKey)", example = "D3")
	String ruleKey,

	@Schema(description = "체험 ID (data.experienceId, FCM data는 문자열)", example = "10")
	Long experienceId,

	@Schema(description = "탭 시 이동 화면 (data.screen). 기본값 experience_detail", example = "experience_detail")
	String screen
) {

	public static final String DEFAULT_SCREEN = "experience_detail";

	public PushMessage {
		if (screen == null || screen.isBlank()) {
			screen = DEFAULT_SCREEN;
		}
	}

	public Map<String, String> data() {
		Map<String, String> payload = new LinkedHashMap<>();
		payload.put("ruleKey", ruleKey);
		payload.put("experienceId", experienceId == null ? null : experienceId.toString());
		payload.put("screen", screen);
		return payload;
	}
}
