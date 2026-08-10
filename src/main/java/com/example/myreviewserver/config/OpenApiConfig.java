package com.example.myreviewserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger baseline + JWT bearer scheme for protected APIs.
 *
 * @Configuration: marks this class as a source of Spring beans.
 * @Bean: registers the returned OpenAPI object in the Spring context.
 */
@Configuration
public class OpenApiConfig {

	public static final String BEARER_AUTH_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI myReviewOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("My Review Server API")
				.description("""
					my_review_server API 문서.

					소셜 로그인(`POST /api/auth/*`)으로 JWT를 받은 뒤,
					Swagger 우측 상단 Authorize에 accessToken 값만 붙여넣으면 됩니다.
					(UI가 자동으로 Bearer 접두사를 붙입니다.)
					""")
				.version("v0.0.1"))
			.components(new Components()
				.addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
					.name(BEARER_AUTH_SCHEME)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("소셜 로그인 응답의 accessToken 값만 입력 (Bearer 접두사 없이)")));
	}
}
