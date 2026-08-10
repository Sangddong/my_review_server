package com.example.myreviewserver.adapter.inbound.web.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.application.auth.naver.NaverOAuthClient;
import com.example.myreviewserver.application.auth.naver.NaverUserProfile;
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
class NaverAuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	NaverOAuthClient naverOAuthClient;

	@Test
	void logsInWithNaverCode() throws Exception {
		when(naverOAuthClient.fetchUserProfile(eq("auth-code"), eq("csrf-state")))
			.thenReturn(new NaverUserProfile("nv-1", "n@test.com", "naverUser"));

		mockMvc.perform(post("/api/auth/naver")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"auth-code","state":"csrf-state"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.userId").isNumber())
			.andExpect(jsonPath("$.data.nickname").value("naverUser"))
			.andExpect(jsonPath("$.data.newlyRegistered").value(true));
	}
}
