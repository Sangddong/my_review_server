package com.example.myreviewserver.adapter.inbound.web.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.myreviewserver.application.auth.kakao.KakaoOAuthClient;
import com.example.myreviewserver.application.auth.kakao.KakaoUserProfile;
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
class KakaoAuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	KakaoOAuthClient kakaoOAuthClient;

	@Test
	void logsInWithKakaoCode() throws Exception {
		when(kakaoOAuthClient.fetchUserProfile(
			eq("auth-code"),
			eq("http://localhost:5173/auth/login/kakao/")
		)).thenReturn(new KakaoUserProfile("kk-1", "k@test.com", "kakaoUser"));

		mockMvc.perform(post("/api/auth/kakao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"auth-code","redirectUri":"http://localhost:5173/auth/login/kakao/"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.userId").isNumber())
			.andExpect(jsonPath("$.data.nickname").value("kakaoUser"))
			.andExpect(jsonPath("$.data.newlyRegistered").value(true));
	}
}
