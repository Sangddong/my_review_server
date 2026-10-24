package com.example.myreviewserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityFilterChainTest.AuthProbeController.class)
class SecurityFilterChainTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Test
	void rejectsProtectedEndpointWithoutToken() throws Exception {
		mockMvc.perform(get("/api/_auth_probe"))
			.andExpect(status().isForbidden());
	}

	@Test
	void allowsProtectedEndpointWithBearerToken() throws Exception {
		User user = userRepository.save(User.create("probe@test.com", "tester"));
		String token = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
		mockMvc.perform(get("/api/_auth_probe")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk());
	}

	@Test
	void allowsLegalDocumentsWithoutAuth() throws Exception {
		mockMvc.perform(get("/legal/privacy"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("/legal/privacy.html"));
		mockMvc.perform(get("/legal/terms"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("/legal/terms.html"));
		mockMvc.perform(get("/privacy"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("/legal/privacy.html"));
		mockMvc.perform(get("/terms"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("/legal/terms.html"));
		MvcResult privacy = mockMvc.perform(get("/legal/privacy.html"))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(new String(privacy.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
			.contains("개인정보처리방침");

		MvcResult terms = mockMvc.perform(get("/legal/terms.html"))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(new String(terms.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
			.contains("이용약관");
	}

	@RestController
	static class AuthProbeController {

		@GetMapping("/api/_auth_probe")
		String probe() {
			return "ok";
		}
	}
}
