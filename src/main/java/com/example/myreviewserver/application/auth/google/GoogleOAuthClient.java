package com.example.myreviewserver.application.auth.google;

/**
 * Port for talking to Google OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface GoogleOAuthClient {

	GoogleUserProfile fetchUserProfile(String authorizationCode, String redirectUri);
}
