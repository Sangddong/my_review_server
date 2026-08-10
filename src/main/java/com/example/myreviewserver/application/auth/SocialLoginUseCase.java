package com.example.myreviewserver.application.auth;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common social login orchestration used by Google/Naver/Kakao adapters later.
 */
@Service
@Transactional
public class SocialLoginUseCase {

	private static final int NICKNAME_MAX_LENGTH = 100;

	private final UserRepository userRepository;
	private final AccessTokenProvider accessTokenProvider;

	public SocialLoginUseCase(UserRepository userRepository, AccessTokenProvider accessTokenProvider) {
		this.userRepository = userRepository;
		this.accessTokenProvider = accessTokenProvider;
	}

	public AuthTokenResult execute(SocialLoginCommand command) {
		validate(command);

		boolean newlyRegistered = false;
		User user = userRepository.findByProvider(command.provider(), command.providerUserId())
			.orElse(null);

		if (user == null) {
			try {
				user = userRepository.save(User.create(command.email(), resolveNickname(command)));
				userRepository.saveOauthAccount(user.getId(), command.provider(), command.providerUserId());
				newlyRegistered = true;
			}
			catch (DataIntegrityViolationException ex) {
				user = userRepository.findByProvider(command.provider(), command.providerUserId())
					.orElseThrow(() -> ex);
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
