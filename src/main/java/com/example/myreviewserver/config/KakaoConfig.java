package com.example.myreviewserver.config;

import com.example.myreviewserver.adapter.outbound.kakao.KakaoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Kakao OAuth configuration properties.
 *
 * @Configuration: Spring 설정 클래스.
 * @EnableConfigurationProperties: KakaoProperties를 빈으로 활성화해 app.kakao.* 바인딩.
 */
@Configuration
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoConfig {
}
