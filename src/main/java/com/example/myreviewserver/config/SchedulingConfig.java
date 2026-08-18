package com.example.myreviewserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled jobs and user purge settings.
 *
 * @Configuration: Spring 설정 클래스.
 * @EnableScheduling: @Scheduled 메서드를 주기 실행.
 * @EnableConfigurationProperties: UserPurgeProperties를 빈으로 활성화.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(UserPurgeProperties.class)
public class SchedulingConfig {
}
