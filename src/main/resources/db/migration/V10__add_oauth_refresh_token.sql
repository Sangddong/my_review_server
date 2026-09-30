-- Store provider refresh token so withdraw can revoke Naver/Google app connections.
ALTER TABLE user_oauth_accounts
  ADD COLUMN refresh_token VARCHAR(512) NULL AFTER provider_user_id;
