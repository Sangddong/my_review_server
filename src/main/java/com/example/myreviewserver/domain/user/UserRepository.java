package com.example.myreviewserver.domain.user;

import java.util.Optional;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(Long id);

	Optional<User> findByProvider(AuthProvider provider, String providerUserId);

	void saveOauthAccount(Long userId, AuthProvider provider, String providerUserId);
}
