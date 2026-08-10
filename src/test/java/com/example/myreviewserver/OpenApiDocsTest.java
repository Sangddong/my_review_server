package com.example.myreviewserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies springdoc OpenAPI endpoint is wired.
 *
 * @AutoConfigureMockMvc: sets up MockMvc without a real HTTP server.
 * @ActiveProfiles("test"): uses H2 test config (Flyway off).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void apiDocsAreAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.openapi").exists())
			.andExpect(jsonPath("$.info.title").value("My Review Server API"))
			.andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
			.andExpect(jsonPath("$.tags[?(@.name=='Auth')]").isArray())
			.andExpect(jsonPath("$.paths['/api/auth/naver'].post").exists())
			.andExpect(jsonPath("$.paths['/api/auth/kakao'].post").exists())
			.andExpect(jsonPath("$.paths['/api/auth/google'].post").exists());
	}
}
