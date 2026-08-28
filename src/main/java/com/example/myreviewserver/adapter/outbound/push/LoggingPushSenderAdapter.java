package com.example.myreviewserver.adapter.outbound.push;

import com.example.myreviewserver.application.notification.PushMessage;
import com.example.myreviewserver.application.notification.PushSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Local stub that logs instead of calling FCM.
 *
 * @Component: Spring 빈으로 등록되어 PushSender 구현체로 주입됨.
 */
@Component
public class LoggingPushSenderAdapter implements PushSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingPushSenderAdapter.class);

	@Override
	public void send(String token, PushMessage message) {
		log.info(
			"Push stub: token={} title={} body={} data={}",
			token,
			message == null ? null : message.title(),
			message == null ? null : message.body(),
			message == null ? null : message.data()
		);
	}
}
