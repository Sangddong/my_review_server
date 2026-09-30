package com.example.myreviewserver.application.user;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates the authenticated user's nickname and returns the saved profile.
 * JWT nickname claim is a login-time snapshot; clients should trust this response
 * (or re-login) after a rename.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdateMyNicknameUseCase {

	private final UserRepository userRepository;

	public UpdateMyNicknameUseCase(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User execute(Long userId, String nickname) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new DomainException("User not found"));
		user.changeNickname(nickname);
		return userRepository.save(user);
	}
}
