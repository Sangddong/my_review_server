package com.example.myreviewserver.domain.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(Long id);

	Optional<User> findByProvider(AuthProvider provider, String providerUserId);

	void saveOauthAccount(Long userId, AuthProvider provider, String providerUserId, String refreshToken);

	List<UserOauthLink> findOauthAccountsByUserId(Long userId);

	/**
	 * Unlinks all OAuth providers for the user so the same social account can register again.
	 */
	void deleteOauthAccountsByUserId(Long userId);

	/**
	 * Deletes the oauth row for a provider identity (e.g. orphaned after soft-delete).
	 */
	void deleteOauthAccount(AuthProvider provider, String providerUserId);

	List<User> findDeletedBefore(Instant cutoff);

	int deleteAllByIdIn(List<Long> userIdList);
}
