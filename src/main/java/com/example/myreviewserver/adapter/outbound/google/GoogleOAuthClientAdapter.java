package com.example.myreviewserver.adapter.outbound.google;

import com.example.myreviewserver.application.auth.google.GoogleOAuthClient;
import com.example.myreviewserver.application.auth.google.GoogleOAuthResult;
import com.example.myreviewserver.application.auth.google.GoogleUserProfile;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls Google token + userinfo + revoke APIs.
 *
 * @Component: Spring 빈으로 등록되어 GoogleOAuthClient 구현체로 주입됨.
 */
@Component
public class GoogleOAuthClientAdapter implements GoogleOAuthClient {

	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String REVOKE_URL = "https://oauth2.googleapis.com/revoke";
	private static final String PROFILE_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

	private final GoogleProperties googleProperties;
	private final RestClient restClient;

	@Autowired
	public GoogleOAuthClientAdapter(GoogleProperties googleProperties) {
		this(googleProperties, RestClient.create());
	}

	GoogleOAuthClientAdapter(GoogleProperties googleProperties, RestClient restClient) {
		this.googleProperties = googleProperties;
		this.restClient = restClient;
	}

	@Override
	public GoogleOAuthResult authenticate(String authorizationCode, String redirectUri) {
		ensureConfigured();
		ensureRedirectAllowed(redirectUri);
		GoogleTokenResponse token = exchangeCodeForToken(authorizationCode, redirectUri);
		GoogleUserProfile profile = fetchProfile(token.accessToken());
		return new GoogleOAuthResult(profile, token.accessToken(), blankToNull(token.refreshToken()));
	}

	@Override
	public void unlink(String token) {
		if (token == null || token.isBlank()) {
			throw new DomainException("Google token is required for unlink");
		}

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("token", token.trim());

		try {
			restClient.post()
				.uri(REVOKE_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to unlink Google account", ex);
		}
	}

	private void ensureConfigured() {
		if (googleProperties.getClientId() == null || googleProperties.getClientId().isBlank()
			|| googleProperties.getClientSecret() == null || googleProperties.getClientSecret().isBlank()) {
			throw new DomainException("Google OAuth is not configured");
		}
	}

	private void ensureRedirectAllowed(String redirectUri) {
		List<String> allowed = googleProperties.getRedirectUris();
		if (allowed == null || allowed.isEmpty()) {
			throw new DomainException("Google redirect URIs are not configured");
		}
		if (!allowed.contains(redirectUri)) {
			throw new DomainException("redirectUri is not allowed");
		}
	}

	private GoogleTokenResponse exchangeCodeForToken(String authorizationCode, String redirectUri) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", googleProperties.getClientId());
		form.add("client_secret", googleProperties.getClientSecret());
		form.add("redirect_uri", redirectUri);
		form.add("code", authorizationCode);

		GoogleTokenResponse body;
		try {
			body = restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(GoogleTokenResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to exchange Google authorization code", ex);
		}

		if (body == null) {
			throw new DomainException("Empty response from Google token API");
		}
		if (body.error() != null && !body.error().isBlank()) {
			String description = body.errorDescription() != null ? body.errorDescription() : body.error();
			throw new DomainException("Google token error: " + description);
		}
		if (body.accessToken() == null || body.accessToken().isBlank()) {
			throw new DomainException("Google access_token is missing");
		}
		return body;
	}

	private GoogleUserProfile fetchProfile(String accessToken) {
		GoogleProfileResponse body;
		try {
			body = restClient.get()
				.uri(PROFILE_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(GoogleProfileResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to fetch Google profile", ex);
		}

		if (body == null || body.sub() == null || body.sub().isBlank()) {
			throw new DomainException("Google profile sub is missing");
		}

		String nickname = blankToNull(body.name());
		if (nickname == null) {
			nickname = blankToNull(body.givenName());
		}

		return new GoogleUserProfile(body.sub(), blankToNull(body.email()), nickname);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	record GoogleTokenResponse(
		String access_token,
		String refresh_token,
		String error,
		String error_description
	) {
		String accessToken() {
			return access_token;
		}

		String refreshToken() {
			return refresh_token;
		}

		String errorDescription() {
			return error_description;
		}
	}

	record GoogleProfileResponse(
		String sub,
		String email,
		String name,
		String given_name
	) {
		String givenName() {
			return given_name;
		}
	}
}
