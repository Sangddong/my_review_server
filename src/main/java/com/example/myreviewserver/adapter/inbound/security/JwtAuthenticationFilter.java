package com.example.myreviewserver.adapter.inbound.security;

import com.example.myreviewserver.domain.user.User;
import com.example.myreviewserver.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads Bearer JWT and populates SecurityContext.
 *
 * @Component: registered as a Spring bean (used by SecurityFilterChain).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = resolveBearerToken(request);
		if (token != null && jwtTokenProvider.validate(token)
			&& SecurityContextHolder.getContext().getAuthentication() == null) {
			UserPrincipal tokenPrincipal = jwtTokenProvider.toPrincipal(token);
			User user = userRepository.findById(tokenPrincipal.getUserId()).orElse(null);
			if (user != null && user.getIsDeleted() == null) {
				UserPrincipal principal = new UserPrincipal(user.getId(), user.getNickname());
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		filterChain.doFilter(request, response);
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		String token = header.substring(7).trim();
		return token.isEmpty() ? null : token;
	}
}
