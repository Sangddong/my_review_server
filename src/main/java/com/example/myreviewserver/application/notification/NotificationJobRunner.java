package com.example.myreviewserver.application.notification;

import java.time.LocalDate;

/**
 * Hook for a concrete notification rule.
 * The caller resolves "today" in the configured zone so every rule compares against
 * the same calendar day that the cron fired on.
 */
public interface NotificationJobRunner {

	void run(LocalDate today);
}
