package com.example.myreviewserver.config;

import com.example.myreviewserver.adapter.outbound.google.GoogleProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Google OAuth configuration properties.
 *
 * @Configuration: Spring 설정 클래스.
 * @EnableConfigurationProperties: GoogleProperties를 빈으로 활성화해 app.google.* 바인딩.
 */
@Configuration
@EnableConfigurationProperties(GoogleProperties.class)
public class GoogleConfig {
}
