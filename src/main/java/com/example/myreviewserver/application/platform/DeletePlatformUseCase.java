package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Soft-deletes an active platform. The row stays so experience links are kept.
 * Missing, other-user, and already-deleted platforms are treated as not found.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class DeletePlatformUseCase {

	private final PlatformRepository platformRepository;

	public DeletePlatformUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public void execute(Long userId, Long platformId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (platformId == null) {
			throw new DomainException("platformId is required");
		}

		Platform platform = platformRepository.findActiveByIdAndUserId(platformId, userId)
			.orElseThrow(() -> new DomainException("Platform not found"));

		platform.softDelete();
		platformRepository.save(platform);
	}
}
