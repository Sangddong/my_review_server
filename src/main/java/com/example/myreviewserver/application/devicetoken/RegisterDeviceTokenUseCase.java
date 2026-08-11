package com.example.myreviewserver.application.devicetoken;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers or updates a push device token for the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: 트랜잭션.
 */
@Service
@Transactional
public class RegisterDeviceTokenUseCase {

	private final DeviceTokenRepository deviceTokenRepository;

	public RegisterDeviceTokenUseCase(DeviceTokenRepository deviceTokenRepository) {
		this.deviceTokenRepository = deviceTokenRepository;
	}

	public DeviceToken execute(Long userId, String token, DevicePlatform platform) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (token == null || token.isBlank()) {
			throw new DomainException("token is required");
		}
		if (platform == null) {
			throw new DomainException("platform is required");
		}

		String normalized = token.trim();
		DeviceToken existing = deviceTokenRepository.findByToken(normalized).orElse(null);
		if (existing != null) {
			existing.reassignTo(userId, platform);
			return deviceTokenRepository.save(existing);
		}

		try {
			return deviceTokenRepository.save(DeviceToken.create(userId, normalized, platform));
		}
		catch (DataIntegrityViolationException ex) {
			DeviceToken raced = deviceTokenRepository.findByToken(normalized)
				.orElseThrow(() -> ex);
			raced.reassignTo(userId, platform);
			return deviceTokenRepository.save(raced);
		}
	}
}
