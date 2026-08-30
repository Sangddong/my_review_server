-- Retention timestamp for soft-deleted platforms (is_deleted=1).
-- Mirrors the users / notifications convention (is_deleted + deleted_at).

ALTER TABLE platforms
  ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL AFTER is_deleted,
  ADD KEY idx_platforms_deleted_at (deleted_at);

-- Existing soft-deleted rows have no recorded deletion time.
-- Soft delete is the last write a platform can receive (updates are blocked once
-- deleted), so updated_at is the closest available approximation.
UPDATE platforms
SET deleted_at = COALESCE(updated_at, created_at)
WHERE is_deleted = 1 AND deleted_at IS NULL;
