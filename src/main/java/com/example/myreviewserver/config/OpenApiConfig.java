package com.example.myreviewserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger baseline.
 * Controllers can add @Tag / @Operation / @Schema in later API issues.
 *
 * @Configuration: marks this class as a source of Spring beans.
 * @Bean: registers the returned OpenAPI object in the Spring context.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI myReviewOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("My Review Server API")
				.description("API documentation for my_review_server")
				.version("v0.0.1"));
	}
}
