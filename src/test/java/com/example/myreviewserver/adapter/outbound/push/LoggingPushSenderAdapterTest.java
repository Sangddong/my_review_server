package com.example.myreviewserver.adapter.outbound.push;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.myreviewserver.application.notification.PushMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class LoggingPushSenderAdapterTest {

	@Test
	void logsNotificationAndDataPayload(CapturedOutput output) {
		LoggingPushSenderAdapter adapter = new LoggingPushSenderAdapter();
		adapter.send("token-a", new PushMessage("제목", "본문", "D3", 10L, PushMessage.DEFAULT_SCREEN));

		assertThat(output.getOut()).contains("token-a");
		assertThat(output.getOut()).contains("title=제목");
		assertThat(output.getOut()).contains("body=본문");
		assertThat(output.getOut()).contains("ruleKey=D3");
		assertThat(output.getOut()).contains("experienceId=10");
		assertThat(output.getOut()).contains("screen=experience_detail");
	}
}
