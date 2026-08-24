package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PushMessageTest {

	@Test
	void buildsDataMapAndDefaultsScreen() {
		PushMessage message = new PushMessage("제목", "본문", "D3", 10L, null);

		assertThat(message.screen()).isEqualTo(PushMessage.DEFAULT_SCREEN);
		assertThat(message.data())
			.containsEntry("ruleKey", "D3")
			.containsEntry("experienceId", "10")
			.containsEntry("screen", "experience_detail");
	}
}
