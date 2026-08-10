-- Align with soft-flag style: NULL = not required, 1 = required
ALTER TABLE experience_platforms
  DROP CHECK chk_experience_platforms_is_required;

ALTER TABLE experience_platforms
  MODIFY COLUMN is_required TINYINT NULL DEFAULT NULL;

ALTER TABLE experience_platforms
  ADD CONSTRAINT chk_experience_platforms_is_required
    CHECK (is_required IS NULL OR is_required = 1);
