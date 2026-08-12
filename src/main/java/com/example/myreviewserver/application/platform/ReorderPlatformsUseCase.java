package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reorders the authenticated user's active platforms.
 * Soft-deleted platforms are ignored and must not appear in orderedIds.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class ReorderPlatformsUseCase {

	private final PlatformRepository platformRepository;

	public ReorderPlatformsUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public List<Platform> execute(Long userId, List<Long> orderedIds) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (orderedIds == null) {
			throw new DomainException("orderedIds is required");
		}
		if (orderedIds.stream().anyMatch(id -> id == null)) {
			throw new DomainException("orderedIds must not contain null");
		}

		List<Platform> active = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
		Set<Long> activeIds = new HashSet<>();
		for (Platform platform : active) {
			activeIds.add(platform.getId());
		}

		Set<Long> requestedIds = new HashSet<>(orderedIds);
		if (requestedIds.size() != orderedIds.size()) {
			throw new DomainException("orderedIds must not contain duplicates");
		}
		if (requestedIds.size() != activeIds.size() || !requestedIds.equals(activeIds)) {
			throw new DomainException("orderedIds must match active platforms exactly");
		}

		for (int index = 0; index < orderedIds.size(); index++) {
			boolean updated = platformRepository.updateActiveSortOrderByIdAndUserId(
				orderedIds.get(index),
				userId,
				index
			);
			if (!updated) {
				throw new DomainException("Platform not found");
			}
		}

		return platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
	}
}
