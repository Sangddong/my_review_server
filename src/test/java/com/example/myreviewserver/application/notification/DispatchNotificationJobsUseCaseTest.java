package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DispatchNotificationJobsUseCaseTest {

	@Autowired
	DispatchNotificationJobsUseCase dispatchNotificationJobsUseCase;

	@Test
	void runsWithNoJobRunners() {
		assertThat(dispatchNotificationJobsUseCase.execute(Instant.parse("2026-08-18T00:00:00Z"))).isEqualTo(0);
	}
}
