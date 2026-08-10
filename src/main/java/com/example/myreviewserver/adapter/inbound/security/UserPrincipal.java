package com.example.myreviewserver.adapter.inbound.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal stored in SecurityContext.
 */
public class UserPrincipal implements UserDetails {

	private final Long userId;
	private final String nickname;

	public UserPrincipal(Long userId, String nickname) {
		this.userId = userId;
		this.nickname = nickname;
	}

	public Long getUserId() {
		return userId;
	}

	public String getNickname() {
		return nickname;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return String.valueOf(userId);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
