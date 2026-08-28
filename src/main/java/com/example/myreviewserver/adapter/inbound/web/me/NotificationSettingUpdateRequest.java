package com.example.myreviewserver.adapter.inbound.web.me;

import com.example.myreviewserver.domain.notification.NotificationRuleKey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "알림 수신 설정 변경 요청")
public record NotificationSettingUpdateRequest(
	@Schema(description = "변경할 규칙 목록 (보내지 않은 규칙은 그대로 유지)")
	List<Item> settingList
) {

	@Schema(name = "NotificationSettingUpdateItem", description = "규칙별 수신 여부")
	public record Item(
		@Schema(description = "알림 규칙 키 (D3, TODAY, OVERDUE)", example = "D3")
		NotificationRuleKey ruleKey,

		@Schema(description = "수신 여부 (true=받음)", example = "false")
		Boolean enabled
	) {
	}
}
