package com.example.myreviewserver.adapter.outbound.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.myreviewserver.application.auth.KakaoUserProfile;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoOAuthClientAdapterTest {

	KakaoProperties properties;
	MockRestServiceServer server;
	KakaoOAuthClientAdapter adapter;

	@BeforeEach
	void setUp() {
		properties = new KakaoProperties();
		properties.setClientId("rest-api-key");
		properties.setClientSecret("client-secret");
		properties.setRedirectUris(List.of("http://localhost:5173/auth/login/kakao/"));
		RestClient.Builder restClientBuilder = RestClient.builder();
		server = MockRestServiceServer.bindTo(restClientBuilder).build();
		adapter = new KakaoOAuthClientAdapter(properties, restClientBuilder.build());
	}

	@Test
	void exchangesCodeAndLoadsProfile() {
		server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("""
				{"access_token":"kakao-access","token_type":"bearer"}
				""", MediaType.APPLICATION_JSON));

		server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header("Authorization", "Bearer kakao-access"))
			.andRespond(withSuccess("""
				{"id":42,"kakao_account":{"email":"a@k.com","profile":{"nickname":"nick"}}}
				""", MediaType.APPLICATION_JSON));

		KakaoUserProfile profile = adapter.fetchUserProfile(
			"code",
			"http://localhost:5173/auth/login/kakao/"
		);

		assertThat(profile.providerUserId()).isEqualTo("42");
		assertThat(profile.email()).isEqualTo("a@k.com");
		assertThat(profile.nickname()).isEqualTo("nick");
		server.verify();
	}

	@Test
	void failsWhenRedirectUriNotAllowed() {
		assertThatThrownBy(() -> adapter.fetchUserProfile("code", "https://evil.example/callback"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not allowed");
	}

	@Test
	void failsWhenNotConfigured() {
		properties.setClientSecret("");
		assertThatThrownBy(() -> adapter.fetchUserProfile("code", "http://localhost:5173/auth/login/kakao/"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not configured");
	}
}
