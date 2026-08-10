-- Soft-deleted platforms keep their name; users may re-add the same name.
-- Uniqueness of active names is enforced in application code if needed.
ALTER TABLE platforms DROP INDEX uk_platforms_user_name;
