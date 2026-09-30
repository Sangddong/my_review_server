package com.example.myreviewserver.application.auth.google;

/**
 * Port for talking to Google OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface GoogleOAuthClient {

	GoogleOAuthResult authenticate(String authorizationCode, String redirectUri);

	/**
	 * Revokes Google OAuth token (access or refresh) to disconnect the app.
	 */
	void unlink(String token);
}
