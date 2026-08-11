package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a platform, or restores a soft-deleted one with the same name.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class CreatePlatformUseCase {

	private final PlatformRepository platformRepository;

	public CreatePlatformUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public Platform execute(Long userId, String name, String color) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (name == null || name.isBlank()) {
			throw new DomainException("name is required");
		}
		String trimmedName = name.trim();

		if (platformRepository.findActiveByUserIdAndName(userId, trimmedName).isPresent()) {
			throw new DomainException("Platform name already exists");
		}

		int sortOrder = platformRepository.findNextSortOrder(userId);
		Platform deleted = platformRepository.findDeletedByUserIdAndName(userId, trimmedName).orElse(null);
		if (deleted != null) {
			deleted.restore(color, sortOrder);
			return platformRepository.save(deleted);
		}

		return platformRepository.save(Platform.create(userId, trimmedName, color, sortOrder));
	}
}
