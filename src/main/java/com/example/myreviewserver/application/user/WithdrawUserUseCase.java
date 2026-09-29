package com.example.myreviewserver.application.user;

import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserOauthLink;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Soft-deletes the authenticated user, unlinks social app connections, stops push,
 * and removes local OAuth rows so the same social account can register again.
 * Hard delete of remaining data is handled later by {@link PurgeWithdrawnUsersUseCase}.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class WithdrawUserUseCase {

	private static final Logger log = LoggerFactory.getLogger(WithdrawUserUseCase.class);

	private final UserRepository userRepository;
	private final DeviceTokenRepository deviceTokenRepository;
	private final SocialAccountUnlinkClient socialAccountUnlinkClient;

	public WithdrawUserUseCase(
		UserRepository userRepository,
		DeviceTokenRepository deviceTokenRepository,
		SocialAccountUnlinkClient socialAccountUnlinkClient
	) {
		this.userRepository = userRepository;
		this.deviceTokenRepository = deviceTokenRepository;
		this.socialAccountUnlinkClient = socialAccountUnlinkClient;
	}

	public void execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new DomainException("User not found"));
		user.ensureActive();

		List<UserOauthLink> oauthLinks = userRepository.findOauthAccountsByUserId(userId);
		for (UserOauthLink link : oauthLinks) {
			socialAccountUnlinkClient.unlink(link.provider(), link.providerUserId());
		}

		user.withdraw(Instant.now());
		userRepository.save(user);

		// Stop push immediately; remaining rows are purged with the user after retention.
		deviceTokenRepository.deleteAllByUserIdIn(List.of(userId));
		// Remove local oauth rows after provider unlink so the same account can re-register.
		userRepository.deleteOauthAccountsByUserId(userId);

		log.info("User withdrawn: userId={}", userId);
	}
}
