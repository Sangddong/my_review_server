package com.example.myreviewserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cron and toggle for the notification dispatcher job.
 *
 * @ConfigurationProperties: app.notification.* 설정을 이 클래스 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

	private boolean enabled = true;

	private String cron = "0 0 9 * * *";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}
}
