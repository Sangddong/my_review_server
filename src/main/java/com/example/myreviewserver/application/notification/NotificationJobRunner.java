package com.example.myreviewserver.application.notification;

import java.time.Instant;

/**
 * Hook for a concrete notification rule. Follow-up issues register implementations.
 */
public interface NotificationJobRunner {

	void run(Instant now);
}
