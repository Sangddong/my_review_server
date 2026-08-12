package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Partially updates an active platform's name and/or color.
 * Missing, other-user, and soft-deleted platforms are treated as not found.
 *
 * @Service: 서비스 빈.
 * @Transactional: DB 트랜잭션.
 */
@Service
@Transactional
public class UpdatePlatformUseCase {

	private final PlatformRepository platformRepository;

	public UpdatePlatformUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public Platform execute(Long userId, Long platformId, String name, String color) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (platformId == null) {
			throw new DomainException("platformId is required");
		}

		boolean hasName = name != null;
		boolean hasColor = color != null;
		if (!hasName && !hasColor) {
			throw new DomainException("name or color is required");
		}

		Platform platform = platformRepository.findActiveByIdAndUserId(platformId, userId)
			.orElseThrow(() -> new DomainException("Platform not found"));

		if (hasName) {
			String trimmedName = name.trim();
			if (!trimmedName.equals(platform.getName())) {
				List<Platform> active = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
				boolean nameExists = active.stream()
					.anyMatch(existing -> existing.getName().equals(trimmedName));
				if (nameExists) {
					throw new DomainException("Platform name already exists");
				}
			}
			platform.rename(name);
		}
		if (hasColor) {
			platform.changeColor(color);
		}

		return platformRepository.save(platform);
	}
}
