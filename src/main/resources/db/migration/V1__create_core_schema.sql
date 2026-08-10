-- Core schema for my_review (MySQL)
-- Naming: snake_case columns, UPPER_SNAKE domain strings, is_* flags
-- Per-user platforms: each user gets copies of platform_templates on signup (app logic later)

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Fixed default platform catalog (shared definition). Not edited by end users.
CREATE TABLE platform_templates (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  color VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_platform_templates_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User-owned platforms (copied from templates; user may edit / soft-hide)
CREATE TABLE platforms (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  color VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  is_hidden TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_platforms_user_name (user_id, name),
  UNIQUE KEY uk_platforms_id_user (id, user_id),
  KEY idx_platforms_user_id (user_id),
  KEY idx_platforms_user_sort (user_id, sort_order, id),
  CONSTRAINT fk_platforms_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_platforms_is_hidden CHECK (is_hidden IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experiences (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  experience_type VARCHAR(20) NOT NULL,
  reservation_date DATE NULL,
  reservation_time TIME NULL,
  review_deadline DATE NOT NULL,
  is_review_submitted TINYINT(1) NOT NULL DEFAULT 0,
  detail_link VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_experiences_id_user (id, user_id),
  KEY idx_experiences_user_id (user_id),
  KEY idx_experiences_user_submitted (user_id, is_review_submitted),
  KEY idx_experiences_reservation_date (reservation_date),
  KEY idx_experiences_review_deadline (review_deadline),
  CONSTRAINT fk_experiences_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_experiences_type
    CHECK (experience_type IN ('VISIT', 'DELIVERY', 'PRESS')),
  CONSTRAINT chk_experiences_is_review_submitted
    CHECK (is_review_submitted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_id duplicated so both FKs enforce the same owner (no cross-user links)
CREATE TABLE experience_platforms (
  experience_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_required TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (experience_id, platform_id),
  KEY idx_experience_platforms_platform_id (platform_id),
  KEY idx_experience_platforms_is_required (is_required),
  KEY idx_experience_platforms_user_id (user_id),
  CONSTRAINT fk_experience_platforms_experience_user
    FOREIGN KEY (experience_id, user_id)
      REFERENCES experiences (id, user_id)
      ON DELETE CASCADE,
  CONSTRAINT fk_experience_platforms_platform_user
    FOREIGN KEY (platform_id, user_id)
      REFERENCES platforms (id, user_id)
      ON DELETE RESTRICT,
  CONSTRAINT chk_experience_platforms_is_required
    CHECK (is_required IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Row present = registration complete for that platform on the experience
CREATE TABLE experience_registered_platforms (
  experience_id BIGINT NOT NULL,
  platform_id BIGINT NOT NULL,
  registered_at DATETIME(3) NOT NULL,
  PRIMARY KEY (experience_id, platform_id),
  KEY idx_experience_registered_platforms_platform_id (platform_id),
  CONSTRAINT fk_experience_registered_platforms_link
    FOREIGN KEY (experience_id, platform_id)
      REFERENCES experience_platforms (experience_id, platform_id)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_templates (id, name, color, sort_order, created_at, updated_at)
VALUES
  (1, '블로그', 'var(--color-chip-blog)', 0, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000'),
  (2, '구글 리뷰', 'var(--color-chip-google)', 1, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000'),
  (3, '클립', 'var(--color-chip-clip)', 2, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000'),
  (4, '릴스', 'var(--color-chip-reels)', 3, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000'),
  (5, '쇼츠', 'var(--color-chip-shorts)', 4, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000'),
  (6, '영수증 리뷰', 'var(--color-chip-receipt)', 5, '1970-01-01 00:00:00.000', '1970-01-01 00:00:00.000');
