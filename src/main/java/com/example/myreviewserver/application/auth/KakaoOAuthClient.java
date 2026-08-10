package com.example.myreviewserver.application.auth;

/**
 * Port for talking to Kakao OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface KakaoOAuthClient {

	KakaoUserProfile fetchUserProfile(String authorizationCode, String redirectUri);
}
