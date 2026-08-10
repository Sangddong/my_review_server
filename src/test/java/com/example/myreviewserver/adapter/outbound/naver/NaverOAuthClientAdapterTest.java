package com.example.myreviewserver.adapter.outbound.naver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.myreviewserver.application.auth.NaverUserProfile;
import com.example.myreviewserver.domain.shared.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NaverOAuthClientAdapterTest {

	NaverProperties properties;
	MockRestServiceServer server;
	NaverOAuthClientAdapter adapter;

	@BeforeEach
	void setUp() {
		properties = new NaverProperties();
		properties.setClientId("client-id");
		properties.setClientSecret("client-secret");
		RestClient.Builder restClientBuilder = RestClient.builder();
		server = MockRestServiceServer.bindTo(restClientBuilder).build();
		adapter = new NaverOAuthClientAdapter(properties, restClientBuilder.build());
	}

	@Test
	void exchangesCodeAndLoadsProfile() {
		server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withSuccess("""
				{"access_token":"naver-access","token_type":"bearer"}
				""", MediaType.APPLICATION_JSON));

		server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header("Authorization", "Bearer naver-access"))
			.andRespond(withSuccess("""
				{"resultcode":"00","message":"success","response":{"id":"nv-42","email":"a@n.com","nickname":"nick"}}
				""", MediaType.APPLICATION_JSON));

		NaverUserProfile profile = adapter.fetchUserProfile("code", "state");

		assertThat(profile.providerUserId()).isEqualTo("nv-42");
		assertThat(profile.email()).isEqualTo("a@n.com");
		assertThat(profile.nickname()).isEqualTo("nick");
		server.verify();
	}

	@Test
	void failsWhenNotConfigured() {
		properties.setClientId("");
		assertThatThrownBy(() -> adapter.fetchUserProfile("code", "state"))
			.isInstanceOf(DomainException.class)
			.hasMessageContaining("not configured");
	}
}
