package com.example.myreviewserver.adapter.inbound.security;

import com.example.myreviewserver.domain.shared.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads the authenticated {@link UserPrincipal} from SecurityContext.
 */
public final class CurrentUser {

	private CurrentUser() {
	}

	public static Long requireUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new DomainException("authentication required");
		}
		return principal.getUserId();
	}
}
