-- Sent push records for duplicate-notification prevention.
-- One row per (experience_id, rule_key); later jobs reuse keys such as D3 / TODAY / OVERDUE.

CREATE TABLE notification_sends (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  experience_id BIGINT NOT NULL,
  rule_key VARCHAR(50) NOT NULL,
  sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_sends_experience_rule (experience_id, rule_key),
  KEY idx_notification_sends_user_id (user_id),
  CONSTRAINT fk_notification_sends_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notification_sends_experience
    FOREIGN KEY (experience_id) REFERENCES experiences (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
