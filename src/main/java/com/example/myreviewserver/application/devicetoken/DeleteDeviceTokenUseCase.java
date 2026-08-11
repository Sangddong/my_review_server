package com.example.myreviewserver.application.devicetoken;

import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deletes a device token belonging to the authenticated user.
 *
 * @Service: 서비스 빈.
 * @Transactional: 트랜잭션.
 */
@Service
@Transactional
public class DeleteDeviceTokenUseCase {

	private final DeviceTokenRepository deviceTokenRepository;

	public DeleteDeviceTokenUseCase(DeviceTokenRepository deviceTokenRepository) {
		this.deviceTokenRepository = deviceTokenRepository;
	}

	public void execute(Long userId, String token) {
		if (userId == null) {
			throw new DomainException("userId is required");
		}
		if (token == null || token.isBlank()) {
			throw new DomainException("token is required");
		}
		boolean deleted = deviceTokenRepository.deleteByUserIdAndToken(userId, token.trim());
		if (!deleted) {
			throw new DomainException("Device token not found");
		}
	}
}
