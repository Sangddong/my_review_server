-- User-facing notification inbox (app 알림 탭).
-- Separate from notification_sends (duplicate-push prevention).

CREATE TABLE notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  experience_id BIGINT NOT NULL,
  rule_key VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  body VARCHAR(500) NOT NULL,
  is_read TINYINT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notifications_user_created (user_id, created_at),
  KEY idx_notifications_user_is_read (user_id, is_read),
  KEY idx_notifications_experience_id (experience_id),
  CONSTRAINT fk_notifications_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_experience
    FOREIGN KEY (experience_id) REFERENCES experiences (id) ON DELETE CASCADE,
  CONSTRAINT chk_notifications_is_read
    CHECK (is_read IS NULL OR is_read = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
