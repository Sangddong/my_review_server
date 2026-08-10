package com.example.myreviewserver.adapter.outbound.naver;

import com.example.myreviewserver.application.auth.NaverOAuthClient;
import com.example.myreviewserver.application.auth.NaverUserProfile;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls Naver token + profile APIs.
 *
 * @Component: Spring 빈으로 등록되어 NaverOAuthClient 구현체로 주입됨.
 */
@Component
public class NaverOAuthClientAdapter implements NaverOAuthClient {

	private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
	private static final String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

	private final NaverProperties naverProperties;
	private final RestClient restClient;

	@Autowired
	public NaverOAuthClientAdapter(NaverProperties naverProperties) {
		this(naverProperties, RestClient.create());
	}

	NaverOAuthClientAdapter(NaverProperties naverProperties, RestClient restClient) {
		this.naverProperties = naverProperties;
		this.restClient = restClient;
	}

	@Override
	public NaverUserProfile fetchUserProfile(String authorizationCode, String state) {
		ensureConfigured();
		String accessToken = exchangeCodeForAccessToken(authorizationCode, state);
		return fetchProfile(accessToken);
	}

	private void ensureConfigured() {
		if (naverProperties.getClientId() == null || naverProperties.getClientId().isBlank()
			|| naverProperties.getClientSecret() == null || naverProperties.getClientSecret().isBlank()) {
			throw new DomainException("Naver OAuth is not configured");
		}
	}

	private String exchangeCodeForAccessToken(String authorizationCode, String state) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", naverProperties.getClientId());
		form.add("client_secret", naverProperties.getClientSecret());
		form.add("code", authorizationCode);
		form.add("state", state);

		NaverTokenResponse body;
		try {
			body = restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(NaverTokenResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to exchange Naver authorization code", ex);
		}

		if (body == null) {
			throw new DomainException("Empty response from Naver token API");
		}
		if (body.error() != null && !body.error().isBlank()) {
			String description = body.errorDescription() != null ? body.errorDescription() : body.error();
			throw new DomainException("Naver token error: " + description);
		}
		if (body.accessToken() == null || body.accessToken().isBlank()) {
			throw new DomainException("Naver access_token is missing");
		}
		return body.accessToken();
	}

	private NaverUserProfile fetchProfile(String accessToken) {
		NaverProfileResponse body;
		try {
			body = restClient.get()
				.uri(PROFILE_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(NaverProfileResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to fetch Naver profile", ex);
		}

		if (body == null) {
			throw new DomainException("Empty response from Naver profile API");
		}
		if (!"00".equals(body.resultcode())) {
			String message = body.message() != null ? body.message() : "unknown";
			throw new DomainException("Naver profile error: " + message);
		}
		if (body.response() == null || body.response().id() == null || body.response().id().isBlank()) {
			throw new DomainException("Naver profile id is missing");
		}

		return new NaverUserProfile(
			body.response().id(),
			blankToNull(body.response().email()),
			blankToNull(body.response().nickname())
		);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	record NaverTokenResponse(
		String access_token,
		String error,
		String error_description
	) {
		String accessToken() {
			return access_token;
		}

		String errorDescription() {
			return error_description;
		}
	}

	record NaverProfileResponse(
		String resultcode,
		String message,
		NaverProfileBody response
	) {
	}

	record NaverProfileBody(
		String id,
		String email,
		String nickname
	) {
	}
}
