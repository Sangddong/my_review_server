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
	void runsRegisteredJobRunners() {
		// D3ReviewDeadlineJobRunner 이 등록되어 있으므로 runner 수 >= 1
		assertThat(dispatchNotificationJobsUseCase.execute(Instant.parse("2026-08-18T00:00:00Z"))).isGreaterThanOrEqualTo(1);
	}
}
