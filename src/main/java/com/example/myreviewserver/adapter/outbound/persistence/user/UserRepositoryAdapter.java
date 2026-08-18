package com.example.myreviewserver.adapter.outbound.persistence.user;

import com.example.myreviewserver.domain.shared.DomainException;
import com.example.myreviewserver.domain.user.AuthProvider;
import com.example.myreviewserver.domain.user.User;
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
	public void saveOauthAccount(Long userId, AuthProvider provider, String providerUserId) {
		oauthAccountRepository.save(UserOauthAccountJpaEntity.of(userId, provider, providerUserId));
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
