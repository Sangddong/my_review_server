package com.example.myreviewserver.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS for SPA / Capacitor WebView.
 * {@code APP_CORS_ORIGINS}: comma-separated origins, or {@code *} (dev only).
 */
@Configuration
public class CorsConfig {

	@Value("${app.cors.origins:*}")
	private String corsOrigins;

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		List<String> origins = Arrays.stream(corsOrigins.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
		boolean wildcard = origins.size() == 1 && "*".equals(origins.get(0));
		if (wildcard) {
			// Credentials cannot pair with literal "*"; pattern is for local/docker only.
			config.addAllowedOriginPattern("*");
			config.setAllowCredentials(false);
		}
		else {
			config.setAllowedOrigins(origins);
			config.setAllowCredentials(true);
		}
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
