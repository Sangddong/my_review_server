package com.example.myreviewserver.application.auth.naver;

/**
 * Port for talking to Naver OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface NaverOAuthClient {

	NaverOAuthResult authenticate(String authorizationCode, String state);

	/**
	 * Revokes Naver app connection using a stored refresh or access token.
	 */
	void unlink(String token, boolean refreshToken);
}
