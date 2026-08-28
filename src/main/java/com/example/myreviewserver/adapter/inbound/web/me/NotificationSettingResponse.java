package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.application.notification.NotificationRuleSetting;
import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 규칙별 수신 설정")
public record NotificationSettingResponse(
	@Schema(description = "알림 규칙 키 (D3, TODAY, OVERDUE)", example = "D3")
	NotificationRuleKey ruleKey,

	@Schema(description = "수신 여부 (true=받음)", example = "true")
	boolean enabled
) {

	public static NotificationSettingResponse from(NotificationRuleSetting setting) {
		return new NotificationSettingResponse(setting.ruleKey(), setting.enabled());
	}
}
