package com.example.myreviewserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
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

					푸시 탭 시 체험 상세 이동 계약은 components.schemas.PushPayload 를 참고한다.
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

	/**
	 * Adds the FCM push tap contract so Swagger shows it even without a REST endpoint.
	 *
	 * @Bean: Spring 빈으로 등록.
	 */
	@Bean
	GlobalOpenApiCustomizer pushPayloadOpenApiCustomizer() {
		return openApi -> {
			Components components = openApi.getComponents();
			if (components == null) {
				components = new Components();
				openApi.setComponents(components);
			}
			components.addSchemas("PushPayload", pushPayloadSchema());
		};
	}

	/**
	 * App tap contract: FCM notification + data for experience detail deep link.
	 */
	private static ObjectSchema pushPayloadSchema() {
		ObjectSchema notification = new ObjectSchema();
		notification.addProperty("title", new StringSchema().example("넥쿨러 리뷰 마감 3일 전입니다"));
		notification.addProperty("body", new StringSchema().example("마감일 전에 리뷰를 작성하여 제출해주세요"));

		ObjectSchema data = new ObjectSchema();
		data.addProperty(
			"ruleKey",
			new StringSchema().example("D3").description("알림 규칙 키 (D3, OVERDUE, TODAY)")
		);
		data.addProperty(
			"experienceId",
			new StringSchema().example("10").description("체험 ID. FCM data 값은 문자열이다.")
		);
		data.addProperty(
			"screen",
			new StringSchema().example("experience_detail").description("탭 시 이동 화면. 기본값 experience_detail")
		);

		ObjectSchema payload = new ObjectSchema();
		payload.setDescription(
			"푸시 탭 시 체험 상세 이동 계약. FCM notification(title/body) + data(ruleKey, experienceId, screen)."
		);
		payload.addProperty("notification", notification);
		payload.addProperty("data", data);
		return payload;
	}
}
