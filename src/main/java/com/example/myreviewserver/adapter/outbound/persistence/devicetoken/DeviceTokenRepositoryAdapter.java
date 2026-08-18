package com.example.myreviewserver.adapter.outbound.persistence.devicetoken;

import com.example.myreviewserver.domain.devicetoken.DeviceToken;
import com.example.myreviewserver.domain.devicetoken.DeviceTokenRepository;
import com.example.myreviewserver.domain.shared.DomainException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbound adapter for device_tokens.
 *
 * @Repository: 영속성 컴포넌트.
 * @Transactional: DB 트랜잭션.
 */
@Repository
@Transactional
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepository {

	private final SpringDataDeviceTokenRepository springDataDeviceTokenRepository;
	private final EntityManager entityManager;

	public DeviceTokenRepositoryAdapter(
		SpringDataDeviceTokenRepository springDataDeviceTokenRepository,
		EntityManager entityManager
	) {
		this.springDataDeviceTokenRepository = springDataDeviceTokenRepository;
		this.entityManager = entityManager;
	}

	@Override
	public DeviceToken save(DeviceToken deviceToken) {
		DeviceTokenJpaEntity entity;
		if (deviceToken.getId() == null) {
			entity = DeviceTokenPersistenceMapper.toNewEntity(deviceToken);
		}
		else {
			entity = springDataDeviceTokenRepository.findById(deviceToken.getId())
				.orElseThrow(() -> new DomainException("Device token not found"));
			DeviceTokenPersistenceMapper.copyToEntity(deviceToken, entity);
		}
		DeviceTokenJpaEntity saved = springDataDeviceTokenRepository.saveAndFlush(entity);
		entityManager.refresh(saved);
		return DeviceTokenPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<DeviceToken> findByToken(String token) {
		return springDataDeviceTokenRepository.findByToken(token)
			.map(DeviceTokenPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DeviceToken> findAllByUserId(Long userId) {
		return springDataDeviceTokenRepository.findByUserIdOrderByIdAsc(userId).stream()
			.map(DeviceTokenPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<DeviceToken> findAllByUserIdIn(List<Long> userIdList) {
		if (userIdList == null || userIdList.isEmpty()) {
			return List.of();
		}
		return springDataDeviceTokenRepository.findByUserIdIn(userIdList).stream()
			.map(DeviceTokenPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	public boolean deleteByUserIdAndToken(Long userId, String token) {
		return springDataDeviceTokenRepository.deleteByUserIdAndToken(userId, token) > 0;
	}

	@Override
	public void deleteAllByUserIdIn(List<Long> userIdList) {
		if (userIdList == null || userIdList.isEmpty()) {
			return;
		}
		springDataDeviceTokenRepository.deleteByUserIdIn(userIdList);
	}
}
