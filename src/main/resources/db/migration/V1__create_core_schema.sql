-- Core schema for my_review (MySQL)
-- Naming: snake_case columns, UPPER_SNAKE domain strings, is_* flags
-- Soft flags: NULL = active/default, 1 = deleted/submitted
-- Timestamps: DB-managed (DEFAULT / ON UPDATE CURRENT_TIMESTAMP)

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(320) NULL,
  nickname VARCHAR(100) NOT NULL,
  is_deleted TINYINT NULL DEFAULT NULL,
  deleted_at TIMESTAMP NULL DEFAULT NULL,
  last_login_at TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_users_email (email),
  KEY idx_users_deleted_at (deleted_at),
  CONSTRAINT chk_users_is_deleted
    CHECK (is_deleted IS NULL OR is_deleted = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Social login identities (GOOGLE / NAVER / KAKAO). Tokens are not stored here.
CREATE TABLE user_oauth_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  provider VARCHAR(20) NOT NULL,
  provider_user_id VARCHAR(191) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_oauth_provider_user (provider, provider_user_id),
  UNIQUE KEY uk_user_oauth_user_provider (user_id, provider),
  KEY idx_user_oauth_accounts_user_id (user_id),
  CONSTRAINT fk_user_oauth_accounts_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_user_oauth_provider
    CHECK (provider IN ('GOOGLE', 'NAVER', 'KAKAO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User-owned platforms (edit / soft-delete per user)
CREATE TABLE platforms (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  color VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  is_deleted TINYINT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_platforms_user_name (user_id, name),
  KEY idx_platforms_user_id (user_id),
  KEY idx_platforms_user_sort (user_id, sort_order, id),
  CONSTRAINT fk_platforms_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_platforms_is_deleted
    CHECK (is_deleted IS NULL OR is_deleted = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experiences (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  experience_type VARCHAR(20) NOT NULL,
  reservation_date DATE NULL,
  reservation_time TIME NULL,
  review_deadline DATE NOT NULL,
  is_review_submitted TINYINT NULL DEFAULT NULL,
  detail_link VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_experiences_user_id (user_id),
  KEY idx_experiences_user_submitted (user_id, is_review_submitted),
  KEY idx_experiences_reservation_date (reservation_date),
  KEY idx_experiences_review_deadline (review_deadline),
  CONSTRAINT fk_experiences_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_experiences_type
    CHECK (experience_type IN ('VISIT', 'DELIVERY', 'PRESS')),
  CONSTRAINT chk_experiences_is_review_submitted
    CHECK (is_review_submitted IS NULL OR is_review_submitted = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experience_platforms (
  experience_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  is_required TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (experience_id, platform_id),
  KEY idx_experience_platforms_platform_id (platform_id),
  KEY idx_experience_platforms_is_required (is_required),
  CONSTRAINT fk_experience_platforms_experience
    FOREIGN KEY (experience_id) REFERENCES experiences (id) ON DELETE CASCADE,
  CONSTRAINT fk_experience_platforms_platform
    FOREIGN KEY (platform_id) REFERENCES platforms (id) ON DELETE RESTRICT,
  CONSTRAINT chk_experience_platforms_is_required
    CHECK (is_required IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Row present = registration complete for that platform on the experience
CREATE TABLE experience_registered_platforms (
  experience_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (experience_id, platform_id),
  KEY idx_experience_registered_platforms_platform_id (platform_id),
  CONSTRAINT fk_experience_registered_platforms_link
    FOREIGN KEY (experience_id, platform_id)
      REFERENCES experience_platforms (experience_id, platform_id)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
