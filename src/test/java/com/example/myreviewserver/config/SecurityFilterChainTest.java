package com.example.myreviewserver.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.adapter.inbound.security.JwtTokenProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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

	@RestController
	static class AuthProbeController {

		@GetMapping("/api/_auth_probe")
		String probe() {
			return "ok";
		}
	}
}
