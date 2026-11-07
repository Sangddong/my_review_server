package com.example.myreviewserver.config;

import com.example.myreviewserver.adapter.outbound.openai.OpenAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables OpenAI configuration properties.
 *
 * @Configuration: Spring 설정 클래스.
 * @EnableConfigurationProperties: OpenAiProperties를 빈으로 활성화해 app.openai.* 바인딩.
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {
}
