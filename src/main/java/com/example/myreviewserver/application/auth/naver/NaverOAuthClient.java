package com.example.myreviewserver.application.auth.naver;

/**
 * Port for talking to Naver OAuth APIs.
 * Implementation lives in the outbound adapter.
 */
public interface NaverOAuthClient {

	NaverUserProfile fetchUserProfile(String authorizationCode, String state);
}
