package com.example.myreviewserver.application.platform;

import com.example.myreviewserver.domain.platform.Platform;
import com.example.myreviewserver.domain.platform.PlatformRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the default platform list for a newly registered user.
 * No-ops when the user already has any active platforms (idempotent).
 *
 * @Service: 서비스 빈으로 등록.
 * @Transactional: DB 트랜잭션 경계.
 */
@Service
@Transactional
public class SeedDefaultPlatformsUseCase {

	private static final List<DefaultPlatform> DEFAULTS = List.of(
		new DefaultPlatform("네이버 블로그", "#03c75a"),
		new DefaultPlatform("릴스", "#e1306c"),
		new DefaultPlatform("숏츠", "#ff0000"),
		new DefaultPlatform("클립", "#00c73c"),
		new DefaultPlatform("네이버 영수증 리뷰", "#2db400"),
		new DefaultPlatform("구글맵 리뷰", "#4285f4"),
		new DefaultPlatform("쇼핑몰 리뷰", "#ff6b35")
	);

	private final PlatformRepository platformRepository;

	public SeedDefaultPlatformsUseCase(PlatformRepository platformRepository) {
		this.platformRepository = platformRepository;
	}

	public void execute(Long userId) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		List<Platform> active = platformRepository.findActiveByUserIdOrderBySortOrderAscIdAsc(userId);
		if (!active.isEmpty()) {
			return;
		}
		int sortOrder = 0;
		for (DefaultPlatform defaults : DEFAULTS) {
			platformRepository.save(
				Platform.create(userId, defaults.name(), defaults.color(), sortOrder++)
			);
		}
	}

	public static List<String> defaultNames() {
		return DEFAULTS.stream().map(DefaultPlatform::name).toList();
	}

	private record DefaultPlatform(String name, String color) {
	}
}
