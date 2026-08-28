-- Soft delete for user notification inbox (is_deleted=1 + deleted_at for retention purge).

ALTER TABLE notifications
  ADD COLUMN is_deleted TINYINT NULL DEFAULT NULL AFTER is_read,
  ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER is_deleted,
  ADD KEY idx_notifications_deleted_at (deleted_at),
  ADD CONSTRAINT chk_notifications_is_deleted
    CHECK (is_deleted IS NULL OR is_deleted = 1);
