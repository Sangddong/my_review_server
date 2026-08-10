package com.example.myreviewserver.adapter.inbound.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Test
	void createsAndValidatesToken() {
		String token = jwtTokenProvider.createAccessToken(10L, "nick");
		assertThat(jwtTokenProvider.validate(token)).isTrue();

		UserPrincipal principal = jwtTokenProvider.toPrincipal(token);
		assertThat(principal.getUserId()).isEqualTo(10L);
		assertThat(principal.getNickname()).isEqualTo("nick");
	}
}
