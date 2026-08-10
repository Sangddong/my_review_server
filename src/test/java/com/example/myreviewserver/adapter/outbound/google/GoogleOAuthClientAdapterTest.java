package com.example.myreviewserver.adapter.outbound.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.myreviewserver.application.auth.google.GoogleUserProfile;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GoogleOAuthClientAdapterTest {

	GoogleProperties properties;
	MockRestServiceServer server;
	GoogleOAuthClientAdapter adapter;

	@BeforeEach
	void setUp() {
		properties = new GoogleProperties();
		properties.setClientId("client-id");
		properties.setClientSecret("client-secret");
		properties.setRedirectUris(List.of("http://localhost:5173/auth/login/google/"));
		RestClient.Builder restClientBuilder = RestClient.builder();
		server = MockRestServiceServer.bindTo(restClientBuilder).build();
		adapter = new GoogleOAuthClientAdapter(properties, restClientBuilder.build());
	}

	@Test
	void exchangesCodeAndLoadsProfile() {
		server.expect(requestTo("https://oauth2.googleapis.com/token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("""
				{"access_token":"google-access","token_type":"Bearer"}
				""", MediaType.APPLICATION_JSON));

		server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header("Authorization", "Bearer google-access"))
			.andRespond(withSuccess("""
				{"sub":"g-42","email":"a@g.com","name":"nick","given_name":"nick"}
				""", MediaType.APPLICATION_JSON));

		GoogleUserProfile profile = adapter.fetchUserProfile(
			"code",
			"http://localhost:5173/auth/login/google/"
		);

		assertThat(profile.providerUserId()).isEqualTo("g-42");
		assertThat(profile.email()).isEqualTo("a@g.com");
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
		assertThatThrownBy(() -> adapter.fetchUserProfile("code", "http://localhost:5173/auth/login/google/"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not configured");
	}
}
