package com.example.myreviewserver.adapter.inbound.security;

import com.example.myreviewserver.application.auth.AccessTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements AccessTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String createAccessToken(Long userId, String nickname) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("nickname", nickname)
			.issuedAt(now)
			.expiration(expiry)
			.signWith(secretKey)
			.compact();
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	public UserPrincipal toPrincipal(String token) {
		Claims claims = parseClaims(token);
		Long userId = Long.valueOf(claims.getSubject());
		String nickname = claims.get("nickname", String.class);
		return new UserPrincipal(userId, nickname);
	}

	@Override
	public long getExpirationMs() {
		return jwtProperties.getExpirationMs();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
