package com.example.myreviewserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the Spring DataSource wiring.
 * Uses H2 (test profile). Local MySQL is verified with profile `local`.
 */
@SpringBootTest
@ActiveProfiles("test")
class DataSourceConnectionTest {

	@Autowired
	DataSource dataSource;

	@Test
	void connectsThroughDataSource() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection.isValid(2)).isTrue();
		}
	}
}
