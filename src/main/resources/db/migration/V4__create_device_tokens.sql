-- Device push tokens (FCM etc.) for authenticated users.
-- users / user_oauth_accounts already created in V1 (GOOGLE|NAVER|KAKAO).
-- experiences.user_id ownership was also established in V1.

CREATE TABLE device_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(512) NOT NULL,
  platform VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_tokens_token (token),
  KEY idx_device_tokens_user_id (user_id),
  KEY idx_device_tokens_user_platform (user_id, platform),
  CONSTRAINT fk_device_tokens_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_device_tokens_platform
    CHECK (platform IN ('ANDROID', 'IOS', 'WEB'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
