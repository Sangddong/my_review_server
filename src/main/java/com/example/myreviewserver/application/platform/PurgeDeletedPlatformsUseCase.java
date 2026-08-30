package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hard-deletes platforms that were soft-deleted before the retention cutoff.
 * Platforms still linked to an experience stay until those experiences are removed.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class PurgeDeletedPlatformsUseCase {

	private static final Logger log = LoggerFactory.getLogger(PurgeDeletedPlatformsUseCase.class);

	private final PlatformRepository platformRepository;

	public PurgeDeletedPlatformsUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public int execute(Instant cutoff) {
		if (cutoff == null) {
			throw new DomainException("cutoff is required");
		}

		int deleted = platformRepository.deleteAllDeletedBefore(cutoff);
		log.info("Hard-deleted {} soft-deleted platforms before {}", deleted, cutoff);
		return deleted;
	}
}
