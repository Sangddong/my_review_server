package com.example.myreviewserver.adapter.inbound.web.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.application.auth.google.GoogleOAuthClient;
import com.example.myreviewserver.application.auth.google.GoogleUserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	GoogleOAuthClient googleOAuthClient;

	@Test
	void logsInWithGoogleCode() throws Exception {
		when(googleOAuthClient.fetchUserProfile(
			eq("auth-code"),
			eq("http://localhost:5173/auth/login/google/")
		)).thenReturn(new GoogleUserProfile("g-1", "g@test.com", "googleUser"));

		mockMvc.perform(post("/api/auth/google")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"auth-code","redirectUri":"http://localhost:5173/auth/login/google/"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.userId").isNumber())
			.andExpect(jsonPath("$.data.nickname").value("googleUser"))
			.andExpect(jsonPath("$.data.newlyRegistered").value(true));
	}
}
