package com.example.myreviewserver.application.notification;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.myreviewserver.domain.shared.DomainException;
import java.time.LocalDate;
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
		assertThat(dispatchNotificationJobsUseCase.execute(LocalDate.of(2026, 8, 18))).isGreaterThanOrEqualTo(1);
	}

	@Test
	void rejectsNullDate() {
		assertThatThrownBy(() -> dispatchNotificationJobsUseCase.execute(null))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("today is required");
	}
}
