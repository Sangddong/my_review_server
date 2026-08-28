package com.example.myreviewserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Retention policy for hard-deleting soft-deleted platforms.
 *
 * @ConfigurationProperties: app.platform.purge.* 설정을 이 클래스 필드에 바인딩.
 */
@ConfigurationProperties(prefix = "app.platform.purge")
public class PlatformPurgeProperties {

	private int afterMonths = 3;

	private String cron = "0 30 4 * * *";

	public int getAfterMonths() {
		return afterMonths;
	}

	public void setAfterMonths(int afterMonths) {
		this.afterMonths = afterMonths;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}
}
