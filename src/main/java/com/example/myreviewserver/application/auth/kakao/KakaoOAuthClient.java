package com.example.myreviewserver.application.auth.kakao;

/**
 * Port for talking to Kakao OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface KakaoOAuthClient {

	KakaoUserProfile fetchUserProfile(String authorizationCode, String redirectUri);

	/**
	 * Disconnects the Kakao app connection for the given Kakao user id (admin key).
	 */
	void unlink(String kakaoUserId);
}
