package com.example.myreviewserver.application.user;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the authenticated user's profile.
 *
 * @Service: 서비스 빈.
 * @Transactional(readOnly = true): 읽기 전용 트랜잭션.
 */
@Service
@Transactional(readOnly = true)
public class GetMyProfileUseCase {

	private final UserRepository userRepository;

	public GetMyProfileUseCase(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new DomainException("User not found"));
		user.ensureActive();
		return user;
	}
}
