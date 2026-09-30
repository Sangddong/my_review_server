package com.example.myreviewserver.adapter.outbound.persistence.user;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserOauthLink;
import com.example.myreviewserver.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRepositoryAdapter implements UserRepository {

	private final SpringDataUserRepository userRepository;
	private final SpringDataUserOauthAccountRepository oauthAccountRepository;
	private final EntityManager entityManager;

	public UserRepositoryAdapter(
		SpringDataUserRepository userRepository,
		SpringDataUserOauthAccountRepository oauthAccountRepository,
		EntityManager entityManager
	) {
		this.userRepository = userRepository;
		this.oauthAccountRepository = oauthAccountRepository;
		this.entityManager = entityManager;
	}

	@Override
	public User save(User user) {
		UserJpaEntity entity;
		if (user.getId() == null) {
			entity = UserPersistenceMapper.toNewEntity(user);
		}
		else {
			entity = userRepository.findById(user.getId())
				.orElseThrow(() -> new DomainException("User not found"));
			UserPersistenceMapper.copyToEntity(user, entity);
		}
		UserJpaEntity saved = userRepository.saveAndFlush(entity);
		entityManager.refresh(saved);
		return UserPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(Long id) {
		return userRepository.findById(id).map(UserPersistenceMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByProvider(AuthProvider provider, String providerUserId) {
		return oauthAccountRepository.findUserByProvider(provider, providerUserId)
			.map(UserPersistenceMapper::toDomain);
	}

	@Override
	public void saveOauthAccount(Long userId, AuthProvider provider, String providerUserId, String refreshToken) {
		Optional<UserOauthAccountJpaEntity> existing =
			oauthAccountRepository.findByUserIdAndProvider(userId, provider);
		if (existing.isPresent()) {
			UserOauthAccountJpaEntity entity = existing.get();
			entity.setProviderUserId(providerUserId);
			if (refreshToken != null && !refreshToken.isBlank()) {
				entity.setRefreshToken(refreshToken.trim());
			}
			oauthAccountRepository.save(entity);
			return;
		}
		oauthAccountRepository.save(
			UserOauthAccountJpaEntity.of(userId, provider, providerUserId, blankToNull(refreshToken))
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOauthLink> findOauthAccountsByUserId(Long userId) {
		if (userId == null) {
			return List.of();
		}
		return oauthAccountRepository.findByUserId(userId).stream()
			.map(entity -> new UserOauthLink(
				entity.getProvider(),
				entity.getProviderUserId(),
				entity.getRefreshToken()
			))
			.toList();
	}

	@Override
	public void deleteOauthAccountsByUserId(Long userId) {
		if (userId == null) {
			return;
		}
		oauthAccountRepository.deleteByUserId(userId);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	@Override
	public void deleteOauthAccount(AuthProvider provider, String providerUserId) {
		if (provider == null || providerUserId == null || providerUserId.isBlank()) {
			return;
		}
		oauthAccountRepository.deleteByProviderAndProviderUserId(provider, providerUserId.trim());
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findDeletedBefore(Instant cutoff) {
		return userRepository.findByIsDeletedAndDeletedAtBefore(1, cutoff).stream()
			.map(UserPersistenceMapper::toDomain)
			.toList();
	}

	@Override
	public int deleteAllByIdIn(List<Long> userIdList) {
		if (userIdList == null || userIdList.isEmpty()) {
			return 0;
		}
		oauthAccountRepository.deleteByUserIdIn(userIdList);
		return (int) userRepository.deleteByIdIn(userIdList);
	}
}
