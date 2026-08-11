package com.example.myreviewserver.domain.devicetoken;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for device push tokens.
 */
public interface DeviceTokenRepository {

	DeviceToken save(DeviceToken deviceToken);

	Optional<DeviceToken> findByToken(String token);

	List<DeviceToken> findAllByUserId(Long userId);

	boolean deleteByUserIdAndToken(Long userId, String token);
}
