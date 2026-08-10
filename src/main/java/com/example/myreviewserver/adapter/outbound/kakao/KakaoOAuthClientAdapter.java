package com.example.myreviewserver.adapter.outbound.kakao;

import com.example.myreviewserver.application.auth.KakaoOAuthClient;
import com.example.myreviewserver.application.auth.KakaoUserProfile;
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
 * Calls Kakao token + user/me APIs.
 *
 * @Component: Spring 빈으로 등록되어 KakaoOAuthClient 구현체로 주입됨.
 */
@Component
public class KakaoOAuthClientAdapter implements KakaoOAuthClient {

	private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
	private static final String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";

	private final KakaoProperties kakaoProperties;
	private final RestClient restClient;

	@Autowired
	public KakaoOAuthClientAdapter(KakaoProperties kakaoProperties) {
		this(kakaoProperties, RestClient.create());
	}

	KakaoOAuthClientAdapter(KakaoProperties kakaoProperties, RestClient restClient) {
		this.kakaoProperties = kakaoProperties;
		this.restClient = restClient;
	}

	@Override
	public KakaoUserProfile fetchUserProfile(String authorizationCode, String redirectUri) {
		ensureConfigured();
		ensureRedirectAllowed(redirectUri);
		String accessToken = exchangeCodeForAccessToken(authorizationCode, redirectUri);
		return fetchProfile(accessToken);
	}

	private void ensureConfigured() {
		if (kakaoProperties.getClientId() == null || kakaoProperties.getClientId().isBlank()
			|| kakaoProperties.getClientSecret() == null || kakaoProperties.getClientSecret().isBlank()) {
			throw new DomainException("Kakao OAuth is not configured");
		}
	}

	private void ensureRedirectAllowed(String redirectUri) {
		List<String> allowed = kakaoProperties.getRedirectUris();
		if (allowed == null || allowed.isEmpty()) {
			throw new DomainException("Kakao redirect URIs are not configured");
		}
		if (!allowed.contains(redirectUri)) {
			throw new DomainException("redirectUri is not allowed");
		}
	}

	private String exchangeCodeForAccessToken(String authorizationCode, String redirectUri) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", kakaoProperties.getClientId());
		form.add("client_secret", kakaoProperties.getClientSecret());
		form.add("redirect_uri", redirectUri);
		form.add("code", authorizationCode);

		KakaoTokenResponse body;
		try {
			body = restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(KakaoTokenResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to exchange Kakao authorization code", ex);
		}

		if (body == null) {
			throw new DomainException("Empty response from Kakao token API");
		}
		if (body.error() != null && !body.error().isBlank()) {
			String description = body.errorDescription() != null ? body.errorDescription() : body.error();
			throw new DomainException("Kakao token error: " + description);
		}
		if (body.accessToken() == null || body.accessToken().isBlank()) {
			throw new DomainException("Kakao access_token is missing");
		}
		return body.accessToken();
	}

	private KakaoUserProfile fetchProfile(String accessToken) {
		KakaoProfileResponse body;
		try {
			body = restClient.get()
				.uri(PROFILE_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(KakaoProfileResponse.class);
		}
		catch (RestClientResponseException ex) {
			throw new DomainException("Failed to fetch Kakao profile", ex);
		}

		if (body == null || body.id() == null) {
			throw new DomainException("Kakao profile id is missing");
		}

		String email = null;
		String nickname = null;
		if (body.kakaoAccount() != null) {
			email = blankToNull(body.kakaoAccount().email());
			if (body.kakaoAccount().profile() != null) {
				nickname = blankToNull(body.kakaoAccount().profile().nickname());
			}
		}
		if (nickname == null && body.properties() != null) {
			nickname = blankToNull(body.properties().nickname());
		}

		return new KakaoUserProfile(String.valueOf(body.id()), email, nickname);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	record KakaoTokenResponse(
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

	record KakaoProfileResponse(
		Long id,
		KakaoAccount kakao_account,
		KakaoPropertiesNickname properties
	) {
		KakaoAccount kakaoAccount() {
			return kakao_account;
		}
	}

	record KakaoAccount(
		String email,
		KakaoProfile profile
	) {
	}

	record KakaoProfile(
		String nickname
	) {
	}

	record KakaoPropertiesNickname(
		String nickname
	) {
	}
}
