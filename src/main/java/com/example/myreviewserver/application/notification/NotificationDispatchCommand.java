package com.example.myreviewserver.application.notification;

/**
 * One push to send for an experience and rule. Later jobs fill this list.
 */
public record NotificationDispatchCommand(
	Long userId,
	Long experienceId,
	String ruleKey,
	String title,
	String body
) {
}
