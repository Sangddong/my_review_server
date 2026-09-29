package com.example.myreviewserver.application.user;

import com.example.myreviewserver.domain.user.AuthProvider;

/**
 * Unlinks the app connection at the social provider (consent / connected services).
 * Local oauth rows are deleted separately by {@link WithdrawUserUseCase}.
 */
public interface SocialAccountUnlinkClient {

	void unlink(AuthProvider provider, String providerUserId);
}
