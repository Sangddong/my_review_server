-- Per-user push preference for each notification rule (D3 / TODAY / OVERDUE).
-- A missing row means the rule is enabled, so defaults need no backfill.

CREATE TABLE notification_settings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  rule_key VARCHAR(20) NOT NULL,
  is_enabled TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_settings_user_rule (user_id, rule_key),
  KEY idx_notification_settings_user_enabled (user_id, is_enabled),
  CONSTRAINT fk_notification_settings_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_notification_settings_rule_key
    CHECK (rule_key IN ('D3', 'TODAY', 'OVERDUE')),
  CONSTRAINT chk_notification_settings_is_enabled
    CHECK (is_enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
