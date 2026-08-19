package com.example.myreviewserver.application.notification;

/**
 * Outbound port for delivering a push. Implementation lives in the adapter.
 */
public interface PushSender {

	void send(String token, PushMessage message);
}
