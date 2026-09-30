package com.example.myreviewserver.application.auth;

import com.example.myreviewserver.application.platform.SeedDefaultPlatformsUseCase;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common social login orchestration used by Google/Naver/Kakao adapters later.
 *
 * @Service: 서비스 빈으로 등록.
 * @Transactional: DB 트랜잭션 경계.
 */
@Service
@Transactional
public class SocialLoginUseCase {

	private static final int NICKNAME_MAX_LENGTH = 100;

	private final UserRepository userRepository;
	private final AccessTokenProvider accessTokenProvider;
	private final SeedDefaultPlatformsUseCase seedDefaultPlatformsUseCase;

	public SocialLoginUseCase(
		UserRepository userRepository,
		AccessTokenProvider accessTokenProvider,
		SeedDefaultPlatformsUseCase seedDefaultPlatformsUseCase
	) {
		this.userRepository = userRepository;
		this.accessTokenProvider = accessTokenProvider;
		this.seedDefaultPlatformsUseCase = seedDefaultPlatformsUseCase;
	}

	public AuthTokenResult execute(SocialLoginCommand command) {
		validate(command);

		boolean newlyRegistered = false;
		User user = userRepository.findByProvider(command.provider(), command.providerUserId())
			.orElse(null);

		if (user == null) {
			try {
				user = registerNewUser(command);
				newlyRegistered = true;
			}
			catch (DataIntegrityViolationException ex) {
				// Concurrent first login, or oauth still pointing at a withdrawn user.
				user = userRepository.findByProvider(command.provider(), command.providerUserId())
					.orElse(null);
				if (user == null) {
					userRepository.deleteOauthAccount(command.provider(), command.providerUserId());
					user = registerNewUser(command);
					newlyRegistered = true;
				}
			}
		}
		else {
			String existingToken = userRepository.findOauthAccountsByUserId(user.getId()).stream()
				.filter(link -> link.provider() == command.provider())
				.map(link -> link.refreshToken())
				.filter(token -> token != null && !token.isBlank())
				.findFirst()
				.orElse(null);
			String revokeToken = resolveRevokeToken(command.refreshToken(), command.accessToken(), existingToken);
			if (revokeToken != null && !revokeToken.equals(existingToken)) {
				userRepository.saveOauthAccount(
					user.getId(),
					command.provider(),
					command.providerUserId(),
					revokeToken
				);
			}
		}

		user.ensureActive();
		user.markLogin();
		user = userRepository.save(user);

		String accessToken = accessTokenProvider.createAccessToken(user.getId(), user.getNickname());
		return new AuthTokenResult(
			accessToken,
			"Bearer",
			accessTokenProvider.getExpirationMs(),
			user.getId(),
			user.getNickname(),
			newlyRegistered
		);
	}

	private User registerNewUser(SocialLoginCommand command) {
		User user = userRepository.save(User.create(command.email(), resolveNickname(command)));
		userRepository.saveOauthAccount(
			user.getId(),
			command.provider(),
			command.providerUserId(),
			resolveRevokeToken(command.refreshToken(), command.accessToken(), null)
		);
		seedDefaultPlatformsUseCase.execute(user.getId());
		return user;
	}

	/**
	 * Prefer provider refresh_token; keep an already-stored token rather than overwriting
	 * it with a short-lived access_token; otherwise fall back to access_token for revoke.
	 */
	private static String resolveRevokeToken(String refreshToken, String accessToken, String existingToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			return refreshToken.trim();
		}
		if (existingToken != null && !existingToken.isBlank()) {
			return existingToken;
		}
		if (accessToken != null && !accessToken.isBlank()) {
			return accessToken.trim();
		}
		return null;
	}

	private void validate(SocialLoginCommand command) {
		if (command == null || command.provider() == null) {
			throw new DomainException("provider is required");
		}
		if (command.providerUserId() == null || command.providerUserId().isBlank()) {
			throw new DomainException("providerUserId is required");
		}
	}

	private String resolveNickname(SocialLoginCommand command) {
		String nickname;
		if (command.nickname() != null && !command.nickname().isBlank()) {
			nickname = command.nickname().trim();
		}
		else if (command.email() != null && command.email().contains("@")) {
			nickname = command.email().substring(0, command.email().indexOf('@'));
		}
		else {
			nickname = command.provider().name().toLowerCase() + "_" + command.providerUserId();
		}
		if (nickname.length() > NICKNAME_MAX_LENGTH) {
			return nickname.substring(0, NICKNAME_MAX_LENGTH);
		}
		return nickname;
	}
}
