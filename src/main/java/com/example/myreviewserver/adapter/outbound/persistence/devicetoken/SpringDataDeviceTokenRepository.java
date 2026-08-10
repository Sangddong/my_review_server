package com.example.myreviewserver.adapter.outbound.persistence.devicetoken;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDeviceTokenRepository extends JpaRepository<DeviceTokenJpaEntity, Long> {

	Optional<DeviceTokenJpaEntity> findByToken(String token);

	List<DeviceTokenJpaEntity> findByUserIdOrderByIdAsc(Long userId);

	long deleteByUserIdAndToken(Long userId, String token);
}
