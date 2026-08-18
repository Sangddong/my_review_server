package com.example.myreviewserver.application.user;

import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.experience.ExperienceRepository;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes withdrawn users whose retention period has elapsed.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class PurgeWithdrawnUsersUseCase {

	private static final Logger log = LoggerFactory.getLogger(PurgeWithdrawnUsersUseCase.class);

	private final UserRepository userRepository;
	private final ExperienceRepository experienceRepository;
	private final PlatformRepository platformRepository;
	private final DeviceTokenRepository deviceTokenRepository;

	public PurgeWithdrawnUsersUseCase(
		UserRepository userRepository,
		ExperienceRepository experienceRepository,
		PlatformRepository platformRepository,
		DeviceTokenRepository deviceTokenRepository
	) {
		this.userRepository = userRepository;
		this.experienceRepository = experienceRepository;
		this.platformRepository = platformRepository;
		this.deviceTokenRepository = deviceTokenRepository;
	}

	public int execute(Instant cutoff) {
		if (cutoff == null) {
			throw new DomainException("cutoff is required");
		}

		List<User> expired = userRepository.findDeletedBefore(cutoff);
		log.info("Purging {} withdrawn users deleted before {}", expired.size(), cutoff);
		if (expired.isEmpty()) {
			return 0;
		}

		List<Long> userIdList = new ArrayList<>();
		for (User user : expired) {
			userIdList.add(user.getId());
		}

		experienceRepository.deleteAllByUserIdIn(userIdList);
		platformRepository.deleteAllByUserIdIn(userIdList);
		deviceTokenRepository.deleteAllByUserIdIn(userIdList);
		int deleted = userRepository.deleteAllByIdIn(userIdList);
		log.info("Hard-deleted withdrawn user ids={}", userIdList);
		return deleted;
	}
}
