package com.example.myreviewserver.adapter.outbound.persistence.user;

import com.example.myreviewserver.domain.user.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_oauth_accounts")
public class UserOauthAccountJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AuthProvider provider;

	@Column(name = "provider_user_id", nullable = false, length = 191)
	private String providerUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected UserOauthAccountJpaEntity() {
	}

	public static UserOauthAccountJpaEntity of(Long userId, AuthProvider provider, String providerUserId) {
		UserOauthAccountJpaEntity entity = new UserOauthAccountJpaEntity();
		entity.userId = userId;
		entity.provider = provider;
		entity.providerUserId = providerUserId;
		return entity;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public AuthProvider getProvider() {
		return provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}
}
