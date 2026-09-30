package com.example.myreviewserver.adapter.outbound.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.myreviewserver.application.auth.google.GoogleOAuthResult;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.hamcrest.Matchers;
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
				{"access_token":"google-access","refresh_token":"google-refresh","token_type":"Bearer"}
				""", MediaType.APPLICATION_JSON));

		server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header("Authorization", "Bearer google-access"))
			.andRespond(withSuccess("""
				{"sub":"g-42","email":"a@g.com","name":"nick","given_name":"nick"}
				""", MediaType.APPLICATION_JSON));

		GoogleOAuthResult auth = adapter.authenticate(
			"code",
			"http://localhost:5173/auth/login/google/"
		);

		assertThat(auth.profile().providerUserId()).isEqualTo("g-42");
		assertThat(auth.profile().email()).isEqualTo("a@g.com");
		assertThat(auth.profile().nickname()).isEqualTo("nick");
		assertThat(auth.refreshToken()).isEqualTo("google-refresh");
		server.verify();
	}

	@Test
	void unlinksWithToken() {
		server.expect(requestTo("https://oauth2.googleapis.com/revoke"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().string(Matchers.containsString("token=google-refresh")))
			.andRespond(withSuccess("", MediaType.APPLICATION_FORM_URLENCODED));

		adapter.unlink("google-refresh");
		server.verify();
	}

	@Test
	void failsWhenRedirectUriNotAllowed() {
		assertThatThrownBy(() -> adapter.authenticate("code", "https://evil.example/callback"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not allowed");
	}

	@Test
	void failsWhenNotConfigured() {
		properties.setClientSecret("");
		assertThatThrownBy(() -> adapter.authenticate("code", "http://localhost:5173/auth/login/google/"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not configured");
	}
}
