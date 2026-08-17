package com.example.myreviewserver.adapter.outbound.persistence.user;

import com.example.myreviewserver.domain.user.AuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataUserOauthAccountRepository extends JpaRepository<UserOauthAccountJpaEntity, Long> {

	@Query("""
		select u from UserJpaEntity u, UserOauthAccountJpaEntity o
		where o.userId = u.id
		  and o.provider = :provider
		  and o.providerUserId = :providerUserId
		""")
	Optional<UserJpaEntity> findUserByProvider(
		@Param("provider") AuthProvider provider,
		@Param("providerUserId") String providerUserId
	);

	long deleteByUserId(Long userId);
}
