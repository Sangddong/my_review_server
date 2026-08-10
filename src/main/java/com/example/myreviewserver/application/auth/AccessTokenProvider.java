package com.example.myreviewserver.application.auth;

/**
 * Port for issuing API access tokens (JWT implementation lives in adapter).
 */
public interface AccessTokenProvider {

	String createAccessToken(Long userId, String nickname);

	long getExpirationMs();
}
