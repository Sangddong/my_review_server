package com.example.myreviewserver.adapter.outbound.persistence.devicetoken;

import com.example.myreviewserver.domain.devicetoken.DevicePlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA mapping for device_tokens table.
 *
 * @Entity: JPA가 관리하는 테이블 행.
 * @Table: DB 테이블 이름.
 * @Enumerated: enum을 문자열(ANDROID 등)로 저장.
 */
@Entity
@Table(name = "device_tokens")
public class DeviceTokenJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false, length = 512)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DevicePlatform platform;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private Instant updatedAt;

	protected DeviceTokenJpaEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DevicePlatform getPlatform() {
		return platform;
	}

	public void setPlatform(DevicePlatform platform) {
		this.platform = platform;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
