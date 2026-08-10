package com.example.myreviewserver.config;

import com.example.myreviewserver.adapter.outbound.naver.NaverProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Naver OAuth configuration properties.
 *
 * @Configuration: Spring 설정 클래스.
 * @EnableConfigurationProperties: NaverProperties를 빈으로 활성화해 app.naver.* 바인딩.
 */
@Configuration
@EnableConfigurationProperties(NaverProperties.class)
public class NaverConfig {
}
