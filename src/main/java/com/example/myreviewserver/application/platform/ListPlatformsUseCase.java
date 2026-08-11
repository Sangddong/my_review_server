package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns the authenticated user's active platforms in display order.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션 (readOnly = 조회만).
 */
@Service
@Transactional(readOnly = true)
public class ListPlatformsUseCase {

	private final PlatformRepository platformRepository;

	public ListPlatformsUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public List<Platform> execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		return platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
	}
}
